package net.johnewart.barista.views;

import io.dropwizard.views.View;
import net.johnewart.barista.core.Cookbook;
import net.johnewart.barista.data.CookbookDAO;

import java.util.List;

public class CookbooksView extends View {

    private final CookbookDAO cookbookDAO;

    public CookbooksView(CookbookDAO cookbookDAO) {
        super("/views/ftl/cookbooks.ftl");
        this.cookbookDAO = cookbookDAO;
    }

    public List<Cookbook> getCookbooks() {
        return cookbookDAO.findAll();
    }

}
