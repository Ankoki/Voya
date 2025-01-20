package us.byeol.voya.activities.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

import us.byeol.voya.R;
import us.byeol.voya.api.Book;
import us.byeol.voya.api.Page;
import us.byeol.voya.misc.Misc;
import us.byeol.voya.misc.popup.PopUp;
import us.byeol.voya.web.IOHandler;

public class PageActivity extends AppCompatActivity {

    /**
     * Sets the preferences to expect an incoming page.
     *
     * @param context the context to set the preferences for.
     * @param uuid the UUID of the incoming book.
     */
    public static void setIncomingPage(Context context, String uuid, int page) {
        SharedPreferences preferences = context.getSharedPreferences("bookdata", 0);
        preferences.edit()
                .putString("current-uuid", uuid)
                .putInt("page", page)
                .apply();
    }

    private CoordinatorLayout coordinator;
    private TextView pageTitle;
    private ImageView pagePhoto;
    private TextView pageContent;
    private ImageButton previousPage;
    private TextView pageCount;
    private ImageButton editPage;
    private ImageButton nextPage;

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
     * @param book the book the page belongs too.
     * @param index the index of the page.
     */
    public void displayPage(Book book, int index) {
        this.setContentView(R.layout.activity_page);
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
            this.pagePhoto = this.findViewById(R.id.page_photo);
            this.pagePhoto.setImageDrawable(null);
            if (photo != null)
                pagePhoto.setImageBitmap(photo);
            String title = page.getTitle();
            this.pageTitle = this.findViewById(R.id.page_title);
            this.pageTitle.setText(title);
            String content = page.getContent();
            TextView pageContent = this.findViewById(R.id.page_content);
            if (!content.isEmpty())
                pageContent.setText(content);
            this.previousPage = this.findViewById(R.id.previous_page);
            this.previousPage.setOnClickListener(event -> {
                if (book.getPages().size() <= (index - 1))
                    this.displayPage(book, index - 1);
                else
                    PopUp.instance.showText(this.coordinator, "This is the first page.", PopUp.Length.LENGTH_SHORT);
            });
            this.pageCount = this.findViewById(R.id.page_count);
            this.pageCount.setText(index /* + "/" + book.getPages().size() */); // Maybe add the comparison. Might look bad.
            // TODO set the text styling for PageActivity#pageCount.
            this.editPage = this.findViewById(R.id.edit_button);
            this.editPage.setOnClickListener(event -> {
                // TODO
            });
            this.nextPage = this.findViewById(R.id.next_page);
            this.nextPage.setOnClickListener(event -> {
                if (book.getPages().size() > (index + 1))
                    return;
                this.displayPage(book, index + 1);
            });
        }

    }

}
