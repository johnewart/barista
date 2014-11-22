package net.johnewart.barista.views;

import io.dropwizard.views.View;

public class DashboardView extends View {
    public DashboardView() {
        super("/views/ftl/index.ftl");
    }
}
