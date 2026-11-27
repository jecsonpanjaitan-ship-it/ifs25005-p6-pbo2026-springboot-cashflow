package org.delcom.ifs25005springbootcashflow.framework.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.progressbar.ProgressBar;

/**
 * Dialog modal standar: header primer (#2563EB), footer abu-abu (#F3F4F6) dengan tombol di kanan
 * (gaya diatur di styles.css lewat class "cf-dialog").
 */
public class AppDialog extends Dialog {

    public AppDialog(String title) {
        setHeaderTitle(title);
        addClassName("cf-dialog");
        setModal(true);
        setCloseOnOutsideClick(false);
    }

    public void addFooterButtons(Component... buttons) {
        getFooter().add(buttons);
    }

    /** ProgressBar indeterminat yang tersembunyi sampai ada proses yang menunggu. */
    public static ProgressBar newProgressBar() {
        ProgressBar bar = new ProgressBar();
        bar.setIndeterminate(true);
        bar.setVisible(false);
        return bar;
    }
}
