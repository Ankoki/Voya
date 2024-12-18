package us.byeol.voya.activities.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import java.util.Map;

import us.byeol.voya.R;
import us.byeol.voya.api.Book;
import us.byeol.voya.api.Page;
import us.byeol.voya.misc.Log;
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
        ImageButton backButton = this.findViewById(R.id.back_button);
        backButton.setOnClickListener(event -> this.startActivity(new Intent(this.getBaseContext(), HomeActivity.class)));
        TextView bookTitle = this.findViewById(R.id.book_title);
        bookTitle.setText(book.getTitle());
        Map<Integer, Page> pages = book.getPages();
        if (pages.isEmpty() || !pages.containsKey(index)) {
            PopUp.instance.showText(this.coordinator, "No content, add content.", PopUp.Length.LENGTH_LONG);
            return;
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
            pageContent.setText(content);
        }
    }

}
