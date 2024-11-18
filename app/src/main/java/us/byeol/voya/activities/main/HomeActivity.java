package us.byeol.voya.activities.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener;
import com.google.android.material.tabs.TabLayout.Tab;

import java.util.UUID;

import us.byeol.voya.R;
import us.byeol.voya.misc.Log;
import us.byeol.voya.misc.Misc;
import us.byeol.voya.misc.VoyaFactory;
import us.byeol.voya.misc.popup.PopUp;
import us.byeol.voya.web.IOHandler;
import us.byeol.voya.api.Book;
import us.byeol.voya.api.User;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Misc.setStatusBarColour(this.getWindow(), this.getResources().getColor(R.color.sage_green));
        this.setContentView(R.layout.activity_home);
        TabLayout tab = this.findViewById(R.id.home_tab);
        tab.addOnTabSelectedListener(new OnTabSelectedListener() {

            @Override
            public void onTabSelected(Tab tab) {
                String text = String.valueOf(tab.getText());
                if (text.isEmpty() || text.equals("null"))
                    return;
                LinearLayout layout = HomeActivity.this.findViewById(R.id.tab_layout);
                layout.removeAllViews();
                SharedPreferences preferences = HomeActivity.this.getApplicationContext().getSharedPreferences("userdata", 0);
                User user = IOHandler.getInstance().fetchUser(preferences.getString("current-uuid", ""));
                if (user == null || !user.isValid()) {
                    PopUp.instance.showText(HomeActivity.this.getCurrentFocus(), HomeActivity.this.getString(R.string.exception_popup), PopUp.Length.LENGTH_LONG);
                    return;
                }
                if (text.equals(HomeActivity.this.getString(R.string.books_tab_name))) {
                    Book[] books = user.getBooks();
                    if (books == null || books.length == 0) {
                        TextView emptyText = new TextView(HomeActivity.this.getApplicationContext());
                        emptyText.setText(HomeActivity.this.getString(R.string.no_books));
                        emptyText.setPadding(20, 75, 20, 50);
                        emptyText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        emptyText.setTextColor(HomeActivity.this.getColor(R.color.sage_green_darker));
                        layout.addView(emptyText);
                    } else
                        for (Book book : books)
                            layout.addView(HomeActivity.this.generateBookCard(book));
                } else if (text.equals(HomeActivity.this.getString(R.string.invites_tab_name))) {

                }
                layout.requestLayout();
                layout.invalidate();
            }

            @Override
            public void onTabUnselected(Tab tab) {}

            @Override
            public void onTabReselected(Tab tab) {
                this.onTabSelected(tab); // We will just reload the data.
            }

        });
        tab.getTabAt(0).select();
    }

    /**
     * Generates a book card for the given book.
     *
     * @param book the book to generate a card from.
     * @return the book card.
     */
    private View generateBookCard(Book book) {
        Context context = this.getApplicationContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        LinearLayout layout = this.findViewById(R.id.tab_layout);
        CardView card = (CardView) inflater.inflate(R.layout.book_card, layout, false);
        ImageView image = card.findViewById(R.id.book_image);
        image.setImageResource(R.drawable.default_avatar_3);
        TextView title = card.findViewById(R.id.title_text);
        title.setText(book.getTitle());
        TextView description = card.findViewById(R.id.description_text);
        description.setText(book.getBlurb());
        TextView author = card.findViewById(R.id.author_text);
        User primary = IOHandler.getInstance().fetchUser(book.getPrimaryAuthor());
        author.setText(primary.getUsername());
        return card;
    }

}
