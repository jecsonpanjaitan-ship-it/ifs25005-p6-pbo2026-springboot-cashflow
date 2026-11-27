package org.delcom.ifs25005springbootcashflow.framework.view;

import org.delcom.ifs25005springbootcashflow.domain.entity.User;
import org.delcom.ifs25005springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25005springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25005springbootcashflow.usecase.UserUseCase;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import java.util.Map;

/** Daftar seluruh pengguna (khusus ADMIN). */
@Route(value = "users", layout = MainLayout.class)
@PageTitle("Pengguna | Cashflow")
public class UsersView extends VerticalLayout implements BeforeEnterObserver {

    private final UserUseCase userUseCase;
    private final IUserRepository userRepository;
    private final Grid<User> grid = new Grid<>(User.class, false);

    public UsersView(UserUseCase userUseCase, IUserRepository userRepository) {
        this.userUseCase = userUseCase;
        this.userRepository = userRepository;
        setSizeFull();
        setPadding(true);

        GridStyles.apply(grid);
        grid.addColumn(User::getId).setHeader("ID").setSortable(true).setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(User::getUsername).setHeader("Username").setSortable(true);
        grid.addColumn(User::getFullName).setHeader("Nama lengkap").setSortable(true).setFlexGrow(1);
        grid.addColumn(u -> u.getRole().name()).setHeader("Role").setSortable(true).setAutoWidth(true);
        grid.addComponentColumn(u -> {
            Button view = new Button("Lihat transaksi", VaadinIcon.EYE.create(), e -> UI.getCurrent().navigate(
                    "user-transactions", QueryParameters.simple(Map.of("userId", String.valueOf(u.getId())))));
            view.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            return view;
        }).setHeader("Aksi").setAutoWidth(true).setFlexGrow(0);

        PageCard card = new PageCard(grid);
        card.setSizeFull();
        add(new H2("Daftar Pengguna"), card);
        expand(card);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        User user = AuthSession.currentUser(userRepository);
        if (user == null) {
            event.forwardTo(LoginView.class);
        } else if (!user.isAdmin()) {
            event.forwardTo(FinanceView.class);
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        grid.setItems(userUseCase.listUsers());
    }
}
