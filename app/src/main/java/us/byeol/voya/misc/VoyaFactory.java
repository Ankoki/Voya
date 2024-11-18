package us.byeol.voya.misc;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import us.byeol.voya.api.Book;
import us.byeol.voya.api.Page;
import us.byeol.voya.api.User;

/**
 * Class for creating new api objects.
 */
public class VoyaFactory {

    /**
     * Creates a new book.
     *
     * @param uuid the uuid of the book.
     * @param title the title.
     * @param blurb the blurb.
     * @param bookPhoto the path to the book photo.
     * @param author the person creating this book.
     * @return the book.
     */
    public static Book createBook(String uuid,
                                  String title,
                                  String blurb,
                                  String bookPhoto,
                                  String author) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("uuid", uuid);
        map.put("title", title);
        map.put("blurb", blurb);
        map.put("book-photo", bookPhoto);
        map.put("primary-author", author);
        String[] authors = new String[1];
        authors[0] = author;
        map.put("authors", Arrays.asList(authors));
        return Book.deserialize(map);
    }

    /**
     * Creates a new page.
     *
     * @param pagePhoto the path to the page photo.
     * @param title the title of the page.
     * @param content the content.
     * @param pageAuthor the author of the page.
     * @return the page.
     */
    public static Page createPage(String pagePhoto, String title, String content, User pageAuthor) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("page-photo", pagePhoto);
        map.put("title", title);
        map.put("content", content);
        map.put("author", pageAuthor.getUuid());
        return Page.deserialize(map);
    }

}
