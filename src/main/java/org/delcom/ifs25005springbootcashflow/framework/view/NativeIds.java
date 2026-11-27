package org.delcom.ifs25005springbootcashflow.framework.view;

import com.vaadin.flow.component.Component;

/** Menetapkan ID pada elemen <input> native di dalam web component Vaadin (untuk otomasi/uji). */
final class NativeIds {

    private NativeIds() {
    }

    static void assignToInput(Component field, String id) {
        field.getElement().executeJs(
                "const f = this; const set = () => { if (f.inputElement) { f.inputElement.id = $0; }"
                        + " else { setTimeout(set, 50); } }; set();", id);
    }
}
