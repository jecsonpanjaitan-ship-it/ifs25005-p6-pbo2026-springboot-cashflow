package org.delcom.ifs25005springbootcashflow.framework.view;

import org.delcom.ifs25005springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25005springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25005springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25005springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25005springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25005springbootcashflow.domain.entity.TransactionTypeFilter;
import org.delcom.ifs25005springbootcashflow.domain.entity.User;
import org.delcom.ifs25005springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25005springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25005springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25005springbootcashflow.usecase.FinanceAiUseCase;
import org.delcom.ifs25005springbootcashflow.usecase.FinanceUseCase;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/** Dashboard transaksi milik pengguna yang login: ringkasan saldo, toolbar, grid, tambah/hapus, AI. */
@Route(value = "", layout = MainLayout.class)
@PageTitle("Transaksi | Cashflow")
public class FinanceView extends VerticalLayout {

    private final FinanceUseCase finance;
    private final FinanceAiUseCase aiUseCase;
    private final FinancePresenter presenter;
    private final FileStorage storage;
    private final IUserRepository userRepository;

    private final SummaryCards summary;
    private final TextField search = new TextField();
    private final Select<TransactionTypeFilter> typeFilter = new Select<>();
    private final Select<SortOption> sort = new Select<>();
    private final TransactionGrid grid;

    public FinanceView(FinanceUseCase finance, FinanceAiUseCase aiUseCase, FinancePresenter presenter,
                       FileStorage storage, IUserRepository userRepository) {
        this.finance = finance;
        this.aiUseCase = aiUseCase;
        this.presenter = presenter;
        this.storage = storage;
        this.userRepository = userRepository;

        setSizeFull();
        setPadding(true);
        summary = new SummaryCards(presenter);
        grid = new TransactionGrid(presenter, storage, this::confirmDelete);

        search.setPlaceholder("Cari keterangan...");
        search.setPrefixComponent(VaadinIcon.SEARCH.create());
        search.setClearButtonVisible(true);
        search.setValueChangeMode(ValueChangeMode.LAZY);
        search.addValueChangeListener(e -> refresh());

        typeFilter.setLabel("Tipe");
        typeFilter.setItems(TransactionTypeFilter.values());
        typeFilter.setItemLabelGenerator(TransactionTypeFilter::label);
        typeFilter.setValue(TransactionTypeFilter.ALL);
        typeFilter.addValueChangeListener(e -> refresh());

        sort.setLabel("Urutan");
        sort.setItems(SortOption.values());
        sort.setItemLabelGenerator(SortOption::label);
        sort.setValue(SortOption.NEWEST_FIRST);
        sort.addValueChangeListener(e -> refresh());

        Button reload = new Button("Muat ulang", VaadinIcon.REFRESH.create(), e -> refresh());
        Button generateAi = new Button("Generate AI", VaadinIcon.MAGIC.create(), e -> openAi());
        generateAi.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        Button addIncome = new Button("Pemasukan", VaadinIcon.PLUS.create(), e -> openForm(TransactionType.INCOME));
        addIncome.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY);
        Button addExpense = new Button("Pengeluaran", VaadinIcon.MINUS.create(),
                e -> openForm(TransactionType.EXPENSE));
        addExpense.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout toolbar = new HorizontalLayout(search, typeFilter, sort, reload, generateAi, addIncome,
                addExpense);
        toolbar.setWidthFull();
        toolbar.setAlignItems(FlexComponent.Alignment.END);
        toolbar.getStyle().set("flex-wrap", "wrap");
        toolbar.addClassName("toolbar");

        VerticalLayout card = new VerticalLayout(toolbar, grid);
        card.addClassName("page-card");
        card.setSizeFull();
        card.setPadding(true);

        add(new H2("Catatan Keuangan"), summary, card);
        expand(card);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        refresh();
    }

    private void refresh() {
        User user = AuthSession.currentUser(userRepository);
        if (user == null) {
            return;
        }
        grid.setItems(finance.listTransactions(user.getId(), search.getValue(), typeFilter.getValue(),
                sort.getValue()));
        summary.update(finance.getBalance(user.getId()));
    }

    private void openForm(TransactionType type) {
        User user = AuthSession.currentUser(userRepository);
        if (user != null) {
            new TransactionFormDialog(type, finance, presenter, storage, user.getId(), this::refresh).open();
        }
    }

    private void openAi() {
        User user = AuthSession.currentUser(userRepository);
        if (user != null) {
            new AiGenerateDialog(aiUseCase, presenter, user.getId(), this::refresh).open();
        }
    }

    private void confirmDelete(Transaction transaction) {
        AppDialog dialog = new AppDialog("Hapus transaksi");
        dialog.add(new Paragraph("Hapus transaksi berikut?"), new Paragraph(presenter.formatTransaction(transaction)));
        Button cancel = new Button("Batal", e -> dialog.close());
        Button delete = new Button("Hapus", e -> {
            delete(transaction);
            dialog.close();
        });
        delete.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        dialog.addFooterButtons(cancel, delete);
        dialog.open();
    }

    private void delete(Transaction transaction) {
        User user = AuthSession.currentUser(userRepository);
        if (user == null) {
            return;
        }
        try {
            Transaction deleted = finance.deleteTransaction(user.getId(), transaction.getId());
            Notify.success(presenter.deletedMessage(deleted));
            refresh();
        } catch (DomainException ex) {
            Notify.error(ex.getMessage());
        }
    }
}
