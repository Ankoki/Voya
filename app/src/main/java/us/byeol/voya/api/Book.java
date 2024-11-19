package us.byeol.voya.api;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import mx.kenzie.argo.Json;
import us.byeol.voya.misc.Log;
import us.byeol.voya.misc.Misc;
import us.byeol.voya.web.IOHandler;

/**
 * This class controls all bookdata and allows for interaction with users and pages.
 */
public class Book extends MongoMappable {

    /**
     * Deserializes a Book from a map.
     *
     * @param map the map containing the bookdata.
     * @return the Book. May be invalid if any required fields were not found.
     */
    @NonNull
    public static Book deserialize(Map<String, Object> map) {
        Book book = new Book();
        book.valid = book.mapResponse(map);
        return book;
    }

    private String uuid,
                    title,
                    blurb,
                    photoPath;
    private byte[] bookPhoto;
    private String primaryAuthor;
    private final List<String> authors = new ArrayList<>(),
                             readers = new ArrayList<>();
    private final Map<Integer, Page> pages = new HashMap<>();

    private Book() {
        super(book -> IOHandler.getInstance().fetchBook(((Book) book).getUuid()).isValid(),
                book -> IOHandler.getInstance().pushBookdata(((Book) book).getUuid(), book.serialize()));
    }

    /**
     * Gets the UUID of this book.
     *
     * @return the uuid.
     */
    public String getUuid() {
        return this.uuid;
    }

    /**
     * Gets the title of this book.
     *
     * @return the title.
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Sets the title of this book.
     *
     * @param title the new title.
     */
    public void setTitle(String title) {
        this.fetchUpdates();
        this.title = title;
        this.pushChanges();
    }

    /**
     * Gets the blurb of this book.
     *
     * @return the blurb.
     */
    public String getBlurb() {
        return this.blurb;
    }

    /**
     * Sets the blurb of this book.
     *
     * @param blurb the new blurb.
     */
    public void setBlurb(String blurb) {
        this.fetchUpdates();
        this.blurb = blurb;
        this.pushChanges();
    }

    /**
     * Gets the name of the books photo.
     *
     * @return the name of the photo.
     */
    public String getPhotoPath() {
        return this.photoPath;
    }

    /**
     * Gets the profile picture of this book as a Bitmap.
     *
     * @return the profile picture.
     */
    public Bitmap getBookPhoto() {
        return BitmapFactory.decodeByteArray(this.bookPhoto, 0, this.bookPhoto.length);
    }

    /**
     * Sets the profile picture of this book.
     *
     * @param bytes the new image.
     */
    public void setBookPhoto(byte[] bytes) {
        this.fetchUpdates();
        this.bookPhoto = bytes;
        this.pushChanges();
    }

    /**
     * Gets the original author of this book's uuid.<br>
     * Please note this user is <strong>unchecked</strong>.
     * If the user is not valid, the username will not appear.
     *
     * @return the primary author.
     */
    public String getPrimaryAuthor() {
        return this.primaryAuthor;
    }

    /**
     * Whether or not the given user is an author of this book.
     *
     * @param user the user.
     * @return true if is an author.
     */
    public boolean isAuthor(User user) {
        return this.authors.contains(user.getUuid()) || this.primaryAuthor.equals(user.getUuid());
    }

    /**
     * Appends a new page to this book.
     *
     * @param page the new page.
     */
    public void appendPage(Page page) {
        this.fetchUpdates();
        int highest = 0;
        for (Map.Entry<Integer, Page> entry : this.pages.entrySet())
            if (entry.getKey() > highest)
                highest = entry.getKey();
        highest++;
        this.pages.put(highest, page);
        this.pushChanges();
    }

    /**
     * Updates this users userdata with a map.
     * Please be careful using this method, it can cause breaking behaviours if keys are forgotten.
     *
     * @param map the map.
     */
    @SuppressWarnings("unchecked")
    @Override
    protected boolean mapResponse(Map<String, Object> map) {
        if (map == null ||
                !map.containsKey("uuid") ||
                !map.containsKey("title") ||
                !map.containsKey("blurb") ||
                !map.containsKey("book-photo") ||
                !map.containsKey("primary-author"))
            return false;
        this.authors.clear();
        this.readers.clear();
        this.pages.clear();
        this.uuid = Misc.castKey(map, "uuid", String.class);
        this.title = Misc.castKey(map, "title", String.class);
        this.blurb = Misc.castKey(map, "blurb", String.class);
        this.photoPath = Misc.castKey(map, "book-photo", String.class);
        this.primaryAuthor = Misc.castKey(map, "primary-author", String.class);
        if (map.containsKey("authors")) {
            List<String> authors = Misc.castKey(map, "authors", List.class);
            if (authors != null)
                this.authors.addAll(authors);
        }
        if (map.containsKey("readers")) {
            List<String> readers = Misc.castKey(map, "readers", List.class);
            if (readers != null)
                this.readers.addAll(readers);
        }
        if (map.containsKey("pages")) {
            Object rawPages = map.get("pages");
            if (rawPages != null) {
                if (rawPages instanceof Map<?,?> rawMap) {
                    if (!rawMap.isEmpty()) {
                        for (int i = 0; i < 100; i++) {
                            Object uncastedPage = rawMap.get(i);
                            if (uncastedPage == null)
                                break;
                            try {
                                Map<String, Object> rawPage = (Map<String, Object>) uncastedPage;
                                Page page = Page.deserialize(rawPage);
                                this.pages.put(i, page);
                            } catch (Exception ex) {
                                Log.error(ex);
                                break;
                            }
                        }
                    }
                }
            }
        }
        this.bookPhoto = IOHandler.getInstance().getImage(IOHandler.BOOK_IMAGE, this.photoPath);
        return true;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("uuid", this.uuid);
        map.put("title", this.title);
        map.put("blurb", this.blurb);
        map.put("book-photo", this.photoPath);
        map.put("primary-author", this.primaryAuthor);
        map.put("authors", this.authors);
        map.put("readers", this.readers);
        Map<String, Object> pages = new LinkedHashMap<>();
        for (Map.Entry<Integer, Page> entry : this.pages.entrySet())
            pages.put(entry.getKey().toString(), entry.getValue().serialize());
        map.put("pages", pages);
        return map;
    }

    @NonNull
    @Override
    public String toString() {
        return "Book[uuid=" + this.uuid + ",\n" +
                "title=" + this.title + ",\n" +
                "blurb=" + this.blurb + ",\n" +
                "photoPath=" + this.photoPath + ",\n" +
                "primaryAuthor=" + this.primaryAuthor + ",\n" +
                "authors=[" + String.join(", ", this.authors) + "],\n" +
                "readers=[" + String.join(", ", this.readers) + "],\n" +
                "pages=[" + Json.toJson(this.pages) + "]\n" +
                "]";
    }

}
