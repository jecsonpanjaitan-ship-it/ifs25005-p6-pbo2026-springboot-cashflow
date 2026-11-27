package org.delcom.ifs25005springbootcashflow.framework.view;

import org.delcom.ifs25005springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25005springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25005springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25005springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25005springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25005springbootcashflow.usecase.FinanceUseCase;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.UploadHandler;

/** Form tambah pemasukan/pengeluaran + upload bukti opsional. */
public class TransactionFormDialog extends AppDialog {

    private final FinanceUseCase finance;
    private final FinancePresenter presenter;
    private final FileStorage storage;
    private final int userId;
    private final TransactionType type;
    private final Runnable onSaved;

    private final TextField description = new TextField("Keterangan");
    private final TextField amount = new TextField("Jumlah");
    private final ProgressBar progress = newProgressBar();
    private final Button save = new Button("Simpan");

    private byte[] proofBytes;
    private String proofName;

    public TransactionFormDialog(TransactionType type, FinanceUseCase finance, FinancePresenter presenter,
                                 FileStorage storage, int userId, Runnable onSaved) {
        super("Tambah " + type.label());
        this.type = type;
        this.finance = finance;
        this.presenter = presenter;
        this.storage = storage;
        this.userId = userId;
        this.onSaved = onSaved;
        setWidth("min(520px, 94vw)");

        description.setWidthFull();
        description.setMaxLength(255);
        description.setAutofocus(true);
        amount.setWidthFull();
        amount.setPrefixComponent(new Span("Rp"));
        amount.setHelperText("Angka bulat tanpa titik/koma, mis. 1000000");

        Upload upload = new Upload(UploadHandler.inMemory((metadata, data) -> {
            proofName = metadata.fileName();
            proofBytes = data;
        }));
        upload.setAcceptedFileTypes("image/png", "image/jpeg", "image/gif", "image/webp");
        upload.setMaxFiles(1);
        upload.setMaxFileSize((int) FileStorage.MAX_BYTES);
        upload.setUploadButton(new Button("Pilih bukti (opsional)"));
        upload.getElement().addEventListener("file-remove", e -> {
            proofBytes = null;
            proofName = null;
        });
        Div proofBox = new Div(new Span("Bukti transaksi (gambar, maks 5 MB)"), upload);
        proofBox.addClassName("proof-upload");

        VerticalLayout form = new VerticalLayout(description, amount, proofBox, progress);
        form.setPadding(false);
        add(form);

        Button cancel = new Button("Batal", e -> close());
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.addClickListener(e -> save());
        addFooterButtons(cancel, save);
    }

    private void save() {
        progress.setVisible(true);
        save.setEnabled(false);
        try {
            // Validasi dulu supaya tidak ada file yatim bila input salah.
            long value = finance.validateInput(description.getValue(), amount.getValue());
            String proofPath = proofBytes == null ? null : storage.store(proofName, proofBytes);
            Transaction transaction = finance.addTransaction(userId, description.getValue(), value, type, proofPath);
            Notify.success(presenter.addedMessage(transaction));
            close();
            onSaved.run();
        } catch (DomainException ex) {
            Notify.error(ex.getMessage());
        } finally {
            progress.setVisible(false);
            save.setEnabled(true);
        }
    }
}
