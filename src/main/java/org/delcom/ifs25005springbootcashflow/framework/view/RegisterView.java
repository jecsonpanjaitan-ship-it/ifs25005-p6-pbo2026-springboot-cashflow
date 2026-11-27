package org.delcom.ifs25005springbootcashflow.framework.view;

import org.delcom.ifs25005springbootcashflow.domain.entity.User;
import org.delcom.ifs25005springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25005springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25005springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25005springbootcashflow.usecase.AuthUseCase;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

@Route("register")
@PageTitle("Daftar | Cashflow")
public class RegisterView extends VerticalLayout implements BeforeEnterObserver {

    private final AuthUseCase authUseCase;
    private final IUserRepository userRepository;

    private final TextField fullName = new TextField("Nama lengkap");
    private final TextField username = new TextField("Username");
    private final PasswordField password = new PasswordField("Password");
    private final PasswordField confirm = new PasswordField("Ulangi password");
    private final Button submit = new Button("Daftar");
    private final ProgressBar progress = AppDialog.newProgressBar();

    public RegisterView(AuthUseCase authUseCase, IUserRepository userRepository) {
        this.authUseCase = authUseCase;
        this.userRepository = userRepository;

        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        addClassName("auth-view");

        fullName.setWidthFull();
        username.setWidthFull();
        username.setHelperText("3-30 karakter: huruf, angka, titik, garis bawah, minus");
        password.setWidthFull();
        confirm.setWidthFull();

        submit.setWidthFull();
        submit.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submit.addClickShortcut(Key.ENTER);
        submit.addClickListener(e -> register());

        PageCard card = new PageCard(new H2("Daftar Akun"), fullName, username, password, confirm, progress,
                submit, new RouterLink("Sudah punya akun? Masuk", LoginView.class));
        card.addClassName("auth-card");
        add(card);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (AuthSession.currentUser(userRepository) != null) {
            event.forwardTo(FinanceView.class);
        }
    }

    private void register() {
        if (!password.getValue().equals(confirm.getValue())) {
            Notify.error("Konfirmasi password tidak sama");
            return;
        }
        progress.setVisible(true);
        submit.setEnabled(false);
        try {
            User user = authUseCase.register(username.getValue(), password.getValue(), fullName.getValue());
            AuthSession.login(user);
            Notify.success("Registrasi berhasil. Selamat datang, " + user.getFullName() + "!");
            UI.getCurrent().navigate(FinanceView.class);
        } catch (DomainException ex) {
            Notify.error(ex.getMessage());
        } finally {
            progress.setVisible(false);
            submit.setEnabled(true);
        }
    }
}
