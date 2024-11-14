package us.byeol.voya.activities.main;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener;
import com.google.android.material.tabs.TabLayout.Tab;

import us.byeol.voya.R;
import us.byeol.voya.misc.Log;
import us.byeol.voya.misc.Misc;

public class HomeActivity extends AppCompatActivity {

    private static final ViewGroup.LayoutParams DEFAULT_PARAMETERS = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

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
                if (text.equals(HomeActivity.this.getString(R.string.books_tab_name))) {
                    layout.addView(HomeActivity.this.generateBookCard(HomeActivity.this.getApplicationContext(), "Book One", "Description for book one.", "Ankoki"));
                    layout.addView(HomeActivity.this.generateBookCard(HomeActivity.this.getApplicationContext(), "Book Two", "Description for book two.", "Ankoki"));
                    layout.addView(HomeActivity.this.generateBookCard(HomeActivity.this.getApplicationContext(), "Book Three", "Description for book three.", "Ankoki"));
                    layout.addView(HomeActivity.this.generateBookCard(HomeActivity.this.getApplicationContext(), "Book Four", "Description for book four.", "Ankoki"));
                } else {
                    layout.addView(HomeActivity.this.generateBookCard(HomeActivity.this.getApplicationContext(), "TEST TITLE INVITE", "TEST DESCRIPTION", "Ankoki"));
                    layout.addView(HomeActivity.this.generateBookCard(HomeActivity.this.getApplicationContext(), "TEST TITLE INVITE 2", "TEST DESCRIPTION", "Ankoki"));
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

    private View generateBookCard(Context context, String titleText, String descriptionText, String authorText) {
        LayoutInflater inflater = LayoutInflater.from(context);
        LinearLayout layout = this.findViewById(R.id.tab_layout);
        CardView card = (CardView) inflater.inflate(R.layout.book_card, layout, false);
        ImageView image = card.findViewById(R.id.book_image);
        image.setImageResource(R.drawable.default_avatar_3);
        TextView title = card.findViewById(R.id.title_text);
        title.setText(titleText);
        Log.debug("Title[" + title.getText() + "]");
        TextView description = card.findViewById(R.id.description_text);
        description.setText(descriptionText);
        Log.debug("Description[" + description.getText() + "]");
        TextView author = card.findViewById(R.id.author_text);
        author.setText(authorText);
        Log.debug("Author[" + author.getText() + "]");
        return card;
    }

    // TODO change this to a book implementation class [and add image]
    private View generateTabChild(Context context, String titleText, String descriptionText, String authorText) {
        CardView card = new CardView(context);
        LinearLayout.LayoutParams parameters = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        parameters.setMargins(10, 10, 10, 10);
        card.setLayoutParams(parameters);
        card.setCardElevation(9f);
        card.setMaxCardElevation(15f);
        card.setRadius(25f);
        TableRow tableRow = new TableRow(context);
        tableRow.setLayoutParams(parameters);
        TextView testText = new TextView(context);
        // ImageView imageView = new ImageView(context);
        // imageView.setLayoutParams(parameters);
        // imageView.setImageResource(R.drawable.ic_launcher_background);
        testText.setLayoutParams(parameters);
        testText.setText("TESTING TESTING IMAGE");
        LinearLayout vertical = new LinearLayout(context);
        vertical.setOrientation(LinearLayout.VERTICAL);
        vertical.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 35.0f));
        TextView title = new TextView(context);
        title.setLayoutParams(parameters);
        title.setText(titleText);
        title.setTypeface(title.getTypeface(), Typeface.BOLD);
        TextView description = new TextView(context);
        description.setLayoutParams(parameters);
        description.setText(descriptionText);
        TextView author = new TextView(context);
        author.setLayoutParams(parameters);
        author.setText(authorText);
        vertical.addView(title);
        vertical.addView(description);
        vertical.addView(author);
        tableRow.addView(testText);
        tableRow.addView(vertical);
        card.addView(tableRow);
        return card;
    }

}
