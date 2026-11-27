package org.delcom.ifs25005springbootcashflow.framework.view;

import org.delcom.ifs25005springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25005springbootcashflow.domain.entity.User;
import org.delcom.ifs25005springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25005springbootcashflow.framework.auth.AuthSession;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;

/** Layout aplikasi: sidebar, menu avatar (Profil / Keluar), dan guard login global. */
public class MainLayout extends AppLayout implements BeforeEnterObserver {

    private final IUserRepository userRepository;
    private final FileStorage storage;

    private final SideNav nav = new SideNav();
    private final Avatar avatar = new Avatar();
    private final Span nameLabel = new Span();

    public MainLayout(IUserRepository userRepository, FileStorage storage) {
        this.userRepository = userRepository;
        this.storage = storage;
        setPrimarySection(Section.DRAWER);

        H1 title = new H1("Cashflow");
        title.addClassName("app-title");

        HorizontalLayout identity = new HorizontalLayout(avatar, nameLabel);
        identity.setAlignItems(FlexComponent.Alignment.CENTER);
        identity.setSpacing(true);
        MenuBar menuBar = new MenuBar();
        menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
        var root = menuBar.addItem(identity);
        root.getSubMenu().addItem("Profil", e -> UI.getCurrent().navigate(ProfileView.class));
        root.getSubMenu().addItem("Keluar", e -> logout());

        HorizontalLayout bar = new HorizontalLayout(new DrawerToggle(), title, menuBar);
        bar.setWidthFull();
        bar.setAlignItems(FlexComponent.Alignment.CENTER);
        bar.expand(title);
        bar.addClassName("app-bar");
        addToNavbar(bar);

        addToDrawer(nav);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        User user = AuthSession.currentUser(userRepository);
        if (user == null) {
            event.forwardTo(LoginView.class);
            return;
        }
        nav.removeAll();
        nav.addItem(new SideNavItem("Transaksi", FinanceView.class, VaadinIcon.WALLET.create()));
        if (user.isAdmin()) {
            nav.addItem(new SideNavItem("Pengguna", UsersView.class, VaadinIcon.USERS.create()));
        }
        avatar.setName(user.getFullName());
        avatar.setImage(user.hasPhoto() ? storage.url(user.getPhotoPath()) : null);
        nameLabel.setText(user.getFullName());
    }

    private void logout() {
        AuthSession.logout();
        UI.getCurrent().navigate(LoginView.class);
    }
}
