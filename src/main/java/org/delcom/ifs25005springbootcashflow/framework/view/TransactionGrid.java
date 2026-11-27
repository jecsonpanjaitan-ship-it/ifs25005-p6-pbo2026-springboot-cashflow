package org.delcom.ifs25005springbootcashflow.framework.view;

import org.delcom.ifs25005springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25005springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25005springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25005springbootcashflow.domain.entity.TransactionType;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import java.util.Comparator;
import java.util.function.Consumer;

/** Grid transaksi. onDelete = null membuatnya read-only (tanpa kolom aksi). */
public class TransactionGrid extends Grid<Transaction> {

    public TransactionGrid(FinancePresenter presenter, FileStorage storage, Consumer<Transaction> onDelete) {
        super(Transaction.class, false);
        GridStyles.apply(this);

        addColumn(Transaction::getId).setHeader("ID").setSortable(true).setAutoWidth(true).setFlexGrow(0);
        addColumn(Transaction::getDescription).setHeader("Keterangan").setSortable(true).setFlexGrow(1);
        addComponentColumn(t -> {
            Span amount = new Span(presenter.formatAmount(t.getAmount()));
            amount.addClassName(t.getType() == TransactionType.INCOME ? "amount-income" : "amount-expense");
            return amount;
        }).setHeader("Jumlah").setComparator(Comparator.comparingLong(Transaction::getAmount))
                .setAutoWidth(true).setFlexGrow(0);
        addComponentColumn(t -> {
            Span badge = new Span(t.getType().label());
            badge.addClassNames("type-badge",
                    t.getType() == TransactionType.INCOME ? "badge-income" : "badge-expense");
            return badge;
        }).setHeader("Tipe").setAutoWidth(true).setFlexGrow(0);
        addComponentColumn(t -> {
            if (!t.hasProof()) {
                return new Span("-");
            }
            String url = storage.url(t.getProofPath());
            Image thumb = new Image(url, "Bukti transaksi");
            thumb.addClassName("proof-thumb");
            thumb.addClickListener(e -> new ImagePreviewDialog("Bukti transaksi #" + t.getId(), url).open());
            return thumb;
        }).setHeader("Bukti").setAutoWidth(true).setFlexGrow(0);
        addColumn(t -> presenter.formatDateTime(t.getCreatedAt())).setHeader("Waktu")
                .setComparator(Comparator.comparing(Transaction::getCreatedAt)).setAutoWidth(true).setFlexGrow(0);

        if (onDelete != null) {
            addComponentColumn(t -> {
                Button delete = new Button(VaadinIcon.TRASH.create(), e -> onDelete.accept(t));
                delete.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
                delete.getElement().setAttribute("title", "Hapus");
                delete.getElement().setAttribute("aria-label", "Hapus transaksi " + t.getId());
                return delete;
            }).setHeader("Aksi").setAutoWidth(true).setFlexGrow(0);
        }
    }
}
