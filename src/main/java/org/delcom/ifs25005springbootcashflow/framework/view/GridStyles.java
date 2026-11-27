package org.delcom.ifs25005springbootcashflow.framework.view;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;

/** Gaya grid konsisten di seluruh halaman. */
final class GridStyles {

    private GridStyles() {
    }

    static void apply(Grid<?> grid) {
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
        grid.addClassName("cf-grid");
        grid.setSizeFull();
        grid.setMinHeight("260px");
    }
}
