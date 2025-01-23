package us.byeol.voya.api;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import us.byeol.voya.misc.Misc;
import us.byeol.voya.web.IOHandler;

/**
 * This class controls pages and their contents.
 */
public class Page extends MongoMappable {

    /**
     * Deserializes a Page from a map.
     *
     * @param map the map containing the page data.
     * @return the Page. May be invalid if any required fields were not found.
     */
    @NonNull
    public static Page deserialize(Map<String, Object> map) {
        Page page = new Page();
        page.valid = page.mapResponse(map);
        return page;
    }

    private Page() {
        super(null, null);
    }

    private byte[] pagePhoto;
    private String title,
            content,
            photoPath;
    private String pageAuthor;

    /**
     * Gets the title of this page.
     *
     * @return the title.
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Sets the title of this page.
     *
     * @param title the new title.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets the contents of this page.
     *
     * @return the contents.
     */
    public String getContent() {
        return this.content;
    }

    /**
     * Sets the contents of this page.
     * @param content the new content.
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Gets the name of the books photo.
     *
     * @return the name of the photo.
     */
    public String getPhotoPath() {
        return this.photoPath;
    }

    @Nullable
    public Bitmap getPagePhoto() {
        if (this.photoPath == null || this.photoPath.isEmpty() || this.photoPath.equals("null"))
            return null;
        return BitmapFactory.decodeByteArray(this.pagePhoto, 0, this.pagePhoto.length);
    }

    @Override
    protected boolean mapResponse(Map<String, Object> map) {
        if (map == null)
            return false;
        this.photoPath = Misc.castKey(map, "page-photo", "null", String.class);
        this.title = Misc.castKey(map, "title", "<404> No Title", String.class);
        this.content = Misc.castKey(map, "content", "<404> No Contents", String.class);
        if (this.photoPath != null && !this.photoPath.equals("null"))
            this.pagePhoto = IOHandler.getInstance().getImage(IOHandler.PAGE_IMAGE, this.photoPath);
        String pageAuthor = Misc.castKey(map, "author", String.class);
        if (pageAuthor != null)
            this.pageAuthor = pageAuthor;
        return true;
    }

    /**
     * Overridden as Pages are parts of books.
     */
    @Override
    public void fetchUpdates() {}

    /**
     * Overridden as Pages are parts of books.
     */
    @Override
    public void pushChanges() {}

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("page-photo", this.photoPath);
        map.put("title", this.title);
        map.put("content", this.content);
        map.put("author", this.pageAuthor);
        return map;
    }

    @NonNull
    @Override
    public String toString() {
        return "Page[photoPath=" + this.photoPath + ",\n" +
                "pagePhoto=" + Arrays.toString(this.pagePhoto) + ",\n" +
                "title=" + this.title + ",\n" +
                "content=" + this.content + ",\n" +
                "pageAuthor=" + this.pageAuthor + "\n" +
                "]";
    }

}
