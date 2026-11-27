package org.delcom.ifs25005springbootcashflow.framework.view;

import org.delcom.ifs25005springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25005springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25005springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25005springbootcashflow.domain.entity.TransactionTypeFilter;
import org.delcom.ifs25005springbootcashflow.domain.entity.User;
import org.delcom.ifs25005springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25005springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25005springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25005springbootcashflow.usecase.FinanceUseCase;
import org.delcom.ifs25005springbootcashflow.usecase.UserUseCase;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/** Peninjauan transaksi pengguna lain, read-only (khusus ADMIN). Parameter: ?userId=. */
@Route(value = "user-transactions", layout = MainLayout.class)
@PageTitle("Transaksi Pengguna | Cashflow")
public class UserTransactionsView extends VerticalLayout implements BeforeEnterObserver {

    private final FinanceUseCase finance;
    private final UserUseCase userUseCase;
    private final IUserRepository userRepository;

    private final H2 title = new H2("Transaksi Pengguna");
    private final SummaryCards summary;
    private final TextField search = new TextField();
    private final Select<TransactionTypeFilter> typeFilter = new Select<>();
    private final Select<SortOption> sort = new Select<>();
    private final TransactionGrid grid;

    private int targetUserId;

    public UserTransactionsView(FinanceUseCase finance, UserUseCase userUseCase, FinancePresenter presenter,
                                FileStorage storage, IUserRepository userRepository) {
        this.finance = finance;
        this.userUseCase = userUseCase;
        this.userRepository = userRepository;
        setSizeFull();
        setPadding(true);

        summary = new SummaryCards(presenter);
        grid = new TransactionGrid(presenter, storage, null);

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

        Button back = new Button("Kembali", VaadinIcon.ARROW_LEFT.create(),
                e -> UI.getCurrent().navigate(UsersView.class));
        HorizontalLayout toolbar = new HorizontalLayout(back, search, typeFilter, sort);
        toolbar.setWidthFull();
        toolbar.setAlignItems(FlexComponent.Alignment.END);
        toolbar.getStyle().set("flex-wrap", "wrap");

        VerticalLayout card = new VerticalLayout(toolbar, grid);
        card.addClassName("page-card");
        card.setSizeFull();
        add(title, summary, card);
        expand(card);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        User user = AuthSession.currentUser(userRepository);
        if (user == null) {
            event.forwardTo(LoginView.class);
            return;
        }
        if (!user.isAdmin()) {
            event.forwardTo(FinanceView.class);
            return;
        }
        try {
            targetUserId = event.getLocation().getQueryParameters().getSingleParameter("userId")
                    .map(Integer::parseInt).orElse(0);
            User target = userUseCase.getUser(targetUserId);
            title.setText("Transaksi " + target.getFullName() + " (@" + target.getUsername() + ") - hanya lihat");
        } catch (DomainException | NumberFormatException ex) {
            Notify.error("Pengguna tidak valid");
            event.forwardTo(UsersView.class);
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        refresh();
    }

    private void refresh() {
        if (targetUserId <= 0) {
            return;
        }
        grid.setItems(finance.listTransactions(targetUserId, search.getValue(), typeFilter.getValue(),
                sort.getValue()));
        summary.update(finance.getBalance(targetUserId));
    }
}
