package org.delcom.ifs25005springbootcashflow.framework.view;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

/** Notifikasi seragam. */
final class Notify {

    private Notify() {
    }

    static void success(String message) {
        Notification n = Notification.show(message, 3500, Notification.Position.TOP_END);
        n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    static void error(String message) {
        Notification n = Notification.show(message, 5000, Notification.Position.TOP_END);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
