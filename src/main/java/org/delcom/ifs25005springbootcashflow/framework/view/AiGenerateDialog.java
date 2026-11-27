package org.delcom.ifs25005springbootcashflow.framework.view;

import org.delcom.ifs25005springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25005springbootcashflow.domain.entity.AiGeneratedTransaction;
import org.delcom.ifs25005springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25005springbootcashflow.usecase.FinanceAiUseCase;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextArea;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Generate AI: prompt -> "Proses AI" (ProgressBar terlihat, dikerjakan async + polling UI)
 * -> preview transaksi -> "Tambahkan ke Catatan".
 */
public class AiGenerateDialog extends AppDialog {

    private record Outcome(List<AiGeneratedTransaction> items, String error) {
    }

    private final FinanceAiUseCase aiUseCase;
    private final FinancePresenter presenter;
    private final int userId;
    private final Runnable onSaved;

    private final TextArea prompt = new TextArea("Catatan keuangan");
    private final ProgressBar progress = newProgressBar();
    private final Grid<AiGeneratedTransaction> preview = new Grid<>(AiGeneratedTransaction.class, false);
    private final Button process = new Button("Proses AI");
    private final Button add = new Button("Tambahkan ke Catatan");

    private List<AiGeneratedTransaction> generated = List.of();

    public AiGenerateDialog(FinanceAiUseCase aiUseCase, FinancePresenter presenter, int userId, Runnable onSaved) {
        super("Generate AI");
        this.aiUseCase = aiUseCase;
        this.presenter = presenter;
        this.userId = userId;
        this.onSaved = onSaved;
        setWidth("min(760px, 94vw)");

        prompt.setWidthFull();
        prompt.setMinHeight("120px");
        prompt.setPlaceholder("Contoh: gaji bulan ini 5 juta, bayar kos 1,2 juta, makan siang 25 ribu");

        preview.addColumn(AiGeneratedTransaction::description).setHeader("Keterangan").setFlexGrow(1);
        preview.addColumn(t -> presenter.formatAmount(t.amount())).setHeader("Jumlah").setAutoWidth(true);
        preview.addColumn(t -> t.type().label()).setHeader("Tipe").setAutoWidth(true);
        preview.setAllRowsVisible(true);
        preview.setVisible(false);

        VerticalLayout body = new VerticalLayout(prompt, progress, preview);
        body.setPadding(false);
        add(body);

        Button cancel = new Button("Batal", e -> close());
        process.addClickListener(e -> process());
        add.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        add.setEnabled(false);
        add.addClickListener(e -> saveGenerated());
        addFooterButtons(cancel, process, add);
    }

    private void process() {
        UI ui = UI.getCurrent();
        String text = prompt.getValue();
        setBusy(true);
        ui.setPollInterval(500); // tanpa @Push: polling supaya hasil async tampil & ProgressBar sempat terlihat
        CompletableFuture.supplyAsync(() -> generate(text)).thenAccept(outcome -> ui.access(() -> {
            ui.setPollInterval(-1);
            setBusy(false);
            show(outcome);
        }));
    }

    private Outcome generate(String text) {
        try {
            return new Outcome(aiUseCase.generateTransactions(text), null);
        } catch (DomainException ex) {
            return new Outcome(List.of(), ex.getMessage());
        } catch (RuntimeException ex) {
            return new Outcome(List.of(), "Terjadi kesalahan saat memproses AI: " + ex.getMessage());
        }
    }

    private void show(Outcome outcome) {
        if (outcome.error() != null) {
            generated = List.of();
            preview.setVisible(false);
            add.setEnabled(false);
            Notify.error(outcome.error());
            return;
        }
        generated = outcome.items();
        preview.setItems(generated);
        preview.setVisible(true);
        add.setEnabled(true);
    }

    private void setBusy(boolean busy) {
        progress.setVisible(busy);
        process.setEnabled(!busy);
        process.setText(busy ? "Memproses..." : "Proses AI");
        prompt.setReadOnly(busy);
        add.setEnabled(!busy && !generated.isEmpty());
    }

    private void saveGenerated() {
        try {
            int count = aiUseCase.saveGeneratedTransactions(userId, generated).size();
            Notify.success(presenter.aiSavedMessage(count));
            close();
            onSaved.run();
        } catch (DomainException ex) {
            Notify.error(ex.getMessage());
        }
    }
}
