package us.byeol.voya.activities.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

import us.byeol.voya.R;
import us.byeol.voya.api.Book;
import us.byeol.voya.api.Page;
import us.byeol.voya.api.User;
import us.byeol.voya.misc.Log;
import us.byeol.voya.misc.Misc;
import us.byeol.voya.misc.VoyaFactory;
import us.byeol.voya.misc.popup.PopUp;
import us.byeol.voya.web.IOHandler;

public class PageActivity extends AppCompatActivity {

    /**
     * Sets the preferences to expect an incoming page.
     *
     * @param context the context to set the preferences for.
     * @param uuid    the UUID of the incoming book.
     */
    public static void setIncomingPage(Context context, String uuid, int page) {
        SharedPreferences preferences = context.getSharedPreferences("bookdata", 0);
        preferences.edit()
                .putString("current-uuid", uuid)
                .putInt("page", page)
                .apply();
    }

    private CoordinatorLayout coordinator;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Misc.setStatusBarColour(this.getWindow(), this.getResources().getColor(R.color.sandstone));
        this.setContentView(R.layout.activity_page);
        this.coordinator = this.findViewById(R.id.coordinator);
        SharedPreferences preferences = this.getApplicationContext().getSharedPreferences("bookdata", 0);
        String uuid = preferences.getString("current-uuid", "");
        if (uuid.isEmpty()) {
            PopUp.instance.showText(this.coordinator, "404 No book.", PopUp.Length.LENGTH_LONG);
            this.startActivity(new Intent(this.getBaseContext(), HomeActivity.class));
            return;
        }
        int index = preferences.getInt("page", 0);
        Book book = IOHandler.getInstance().fetchBook(uuid);
        if (book == null || !book.isValid()) {
            PopUp.instance.showText(this.coordinator, "404 No book.", PopUp.Length.LENGTH_LONG);
            this.startActivity(new Intent(this.getBaseContext(), HomeActivity.class));
            return;
        }
        this.displayPage(book, index);
    }

    /**
     * Edit the current page shown to stop the faff of having
     * to create a new activity every time a page is turned.
     * Editing the contents is more performance friendly.
     *
     * @param book  the book the page belongs too.
     * @param index the index of the page.
     */
    public void displayPage(Book book, int index) {
        Log.debug(book.toString());
        this.setContentView(R.layout.activity_page);
        this.coordinator = this.findViewById(R.id.coordinator);
        ImageButton backButton = this.findViewById(R.id.back_button);
        backButton.setOnClickListener(event -> this.startActivity(new Intent(this.getBaseContext(), HomeActivity.class)));
        TextView bookTitle = this.findViewById(R.id.book_title);
        bookTitle.setText(book.getTitle());
        Map<Integer, Page> pages = book.getPages();
        if (pages.isEmpty() || !pages.containsKey(index)) {
            PopUp.instance.showText(this.coordinator, "No content, add content.", PopUp.Length.LENGTH_LONG);
        } else {
            Page page = pages.get(index);
            Bitmap photo = page.getPagePhoto(); // No it won't, thanks to the above checks for if the book is valid.
            ImageView pagePhoto = this.findViewById(R.id.page_photo);
            pagePhoto.setImageDrawable(null);
            if (photo != null)
                pagePhoto.setImageBitmap(photo);
            String title = page.getTitle();
            TextView pageTitle = this.findViewById(R.id.page_title);
            pageTitle.setText(title);
            String content = page.getContent();
            TextView pageContent = this.findViewById(R.id.page_content);
            if (!content.isEmpty())
                pageContent.setText(content);
            ImageButton previousPage = this.findViewById(R.id.previous_page);
            previousPage.setOnClickListener(event -> {
                if ((index - 1) > 0)
                    this.displayPage(book, index - 1);
                else
                    PopUp.instance.showText(this.coordinator, "This is the first page.", PopUp.Length.LENGTH_SHORT);
            });
            TextView pageCount = this.findViewById(R.id.page_count);
            String text = index + "/" + book.getPages().size();
            pageCount.setText(text); // To avoid a warning we declare the text beforehand.
            ImageButton editPage = this.findViewById(R.id.edit_button);
            editPage.setOnClickListener(event -> {
                SharedPreferences preferences = PageActivity.this.getApplicationContext().getSharedPreferences("userdata", 0);
                User user = IOHandler.getInstance().fetchUser(preferences.getString("current-uuid", ""));
                if (!book.isAuthor(user)) {
                    PopUp.instance.showText(this.coordinator, "You are not an author, and cannot edit this book.", PopUp.Length.LENGTH_SHORT);
                    return;
                }
                new AlertDialog.Builder(this)
                        .setTitle("Edit")
                        .setItems(new String[]{"Book Title", "Page Title", /*"Page Photo",*/ "Page Content", "Invite User", "Make User Author", "New Page"}, (dialog, which) -> {
                            LayoutInflater inflater = this.getLayoutInflater();
                            LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.edit_dialog, null);
                            EditText editBox = layout.findViewById(R.id.edit_box);
                            switch (which) {
                                case 0:
                                    editBox.setText(book.getTitle());
                                    new AlertDialog.Builder(this)
                                            .setTitle("Edit Book Title")
                                            .setView(layout)
                                            .setPositiveButton("Done", (innerDialogue, id) -> {
                                                String edited = String.valueOf(editBox.getText());
                                                book.setTitle(edited);
                                                this.displayPage(book, index);
                                            })
                                            .setNegativeButton("Cancel", (innerDialogue, id) -> innerDialogue.cancel())
                                            .show();
                                    break;
                                case 1:
                                    editBox.setText(page.getTitle());
                                    new AlertDialog.Builder(this)
                                            .setTitle("Edit Page Title")
                                            .setView(layout)
                                            .setPositiveButton("Done", (innerDialogue, id) -> {
                                                String edited = String.valueOf(editBox.getText());
                                                page.setTitle(edited);
                                                book.fetchUpdates();
                                                book.pushChanges(); // Pages aren't attached to books and must be fetched and pushed as a part of them.
                                                this.displayPage(book, index);
                                            })
                                            .setNegativeButton("Cancel", (innerDialogue, id) -> innerDialogue.cancel())
                                            .show();
                                    break;
                                case 2:
                                    editBox.setText(page.getContent());
                                    new AlertDialog.Builder(this)
                                            .setTitle("Edit Page Content")
                                            .setView(layout)
                                            .setPositiveButton("Done", (innerDialogue, id) -> {
                                                String edited = String.valueOf(editBox.getText());
                                                page.setContent(edited);
                                                book.fetchUpdates();
                                                book.pushChanges(); // Pages aren't attached to books and must be fetched and pushed as a part of them.
                                                this.displayPage(book, index);
                                            })
                                            .setNegativeButton("Cancel", (innerDialogue, id) -> innerDialogue.cancel())
                                            .show();
                                    break;
                                case 3:
                                    editBox.setHint("Username");
                                    new AlertDialog.Builder(this)
                                            .setTitle("Invite User")
                                            .setView(layout)
                                            .setPositiveButton("Invite", (innerDialogue, id) -> {
                                                String username = String.valueOf(editBox.getText());
                                                String uuid = IOHandler.getInstance().fetchUuid(username);
                                                User loaded = IOHandler.getInstance().fetchUser(uuid);
                                                if (loaded == null || !loaded.isValid()) {
                                                    PopUp.instance.showText(this.coordinator, "Something went wrong [PA179]", PopUp.Length.LENGTH_LONG);
                                                    return;
                                                }
                                                loaded.addBookInvite(book);
                                                PopUp.instance.showText(this.coordinator, username + " has been invited.", PopUp.Length.LENGTH_SHORT);
                                            })
                                            .setNegativeButton("Cancel", (innerDialogue, id) -> innerDialogue.cancel())
                                            .show();
                                    break;
                                case 4:
                                    editBox.setHint("Username");
                                    new AlertDialog.Builder(this)
                                            .setTitle("Make User Author")
                                            .setView(layout)
                                            .setPositiveButton("Done", (innerDialogue, id) -> {
                                                String username = String.valueOf(editBox.getText());
                                                String uuid = IOHandler.getInstance().fetchUuid(username);
                                                if (uuid == null || uuid.isEmpty()) {
                                                    PopUp.instance.showText(this.coordinator, "Something went wrong [PA197]", PopUp.Length.LENGTH_LONG);
                                                    return;
                                                }
                                                if (book.requestAuthorStatus(uuid))
                                                    PopUp.instance.showText(this.coordinator, username + " is now an author.", PopUp.Length.LENGTH_SHORT);
                                                else
                                                    PopUp.instance.showText(this.coordinator, "The request for " + username + " was denied.", PopUp.Length.LENGTH_SHORT);
                                            })
                                            .setNegativeButton("Cancel", (innerDialogue, id) -> innerDialogue.cancel())
                                            .show();
                                case 5:
                                    LinearLayout pageLayout = (LinearLayout) inflater.inflate(R.layout.page_dialogue, null);
                                    EditText titleBox = pageLayout.findViewById(R.id.page_title);
                                    EditText contentBox = pageLayout.findViewById(R.id.page_content);
                                    titleBox.setHint("Title");
                                    contentBox.setHint("Content");
                                    new AlertDialog.Builder(this)
                                            .setTitle("New Page")
                                            .setView(pageLayout)
                                            .setPositiveButton("Create", (innerDialogue, id) -> {
                                                String newTitle = String.valueOf(titleBox.getText());
                                                String newContent = String.valueOf(contentBox.getText());
                                                book.appendPage(VoyaFactory.createPage(null, newTitle, newContent, user));
                                                this.displayPage(book, book.getPages().size());
                                            })
                                            .setNegativeButton("Cancel", (innerDialogue, id) -> innerDialogue.cancel())
                                            .show();
                                default:
                                    PopUp.instance.showText(this.coordinator, "Something went wrong [PA189]", PopUp.Length.LENGTH_LONG);
                            }
                        })
                        .show();
                this.displayPage(book, index);
            });
            ImageButton nextPage = this.findViewById(R.id.next_page);
            nextPage.setOnClickListener(event -> {
                if (book.getPages().size() >= (index + 1))
                    this.displayPage(book, index + 1);
                else
                    PopUp.instance.showText(this.coordinator, "There are no more pages.", PopUp.Length.LENGTH_SHORT);
            });
        }

    }

}
