package org.delcom.ifs25005springbootcashflow.framework.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;

/** Modal preview gambar: klik gambar untuk toggle zoom 1.0x <-> 1.8x; tombol Perkecil/Perbesar/Tutup. */
public class ImagePreviewDialog extends AppDialog {

    private static final double MIN_ZOOM = 0.5;
    private static final double MAX_ZOOM = 3.0;
    private static final double STEP = 0.2;

    private final Image image;
    private double zoom = 1.0;

    public ImagePreviewDialog(String title, String url) {
        super(title);
        setWidth("min(900px, 92vw)");

        image = new Image(url, title);
        image.addClassName("preview-image");
        image.addClickListener(e -> setZoom(zoom > 1.0 ? 1.0 : 1.8));

        Div scroller = new Div(image);
        scroller.addClassName("preview-scroller");
        add(scroller);

        Button zoomOut = new Button("Perkecil", e -> setZoom(Math.max(MIN_ZOOM, zoom - STEP)));
        Button zoomIn = new Button("Perbesar", e -> setZoom(Math.min(MAX_ZOOM, zoom + STEP)));
        Button close = new Button("Tutup", e -> close());
        close.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY);
        addFooterButtons(zoomOut, zoomIn, close);
    }

    private void setZoom(double value) {
        zoom = value;
        image.setWidth(Math.round(zoom * 100) + "%");
    }
}
