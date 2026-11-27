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
import com.vaadin.flow.component.html.Paragraph;
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

@Route("login")
@PageTitle("Masuk | Cashflow")
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final AuthUseCase authUseCase;
    private final IUserRepository userRepository;

    private final TextField username = new TextField("Username");
    private final PasswordField password = new PasswordField("Password");
    private final Button submit = new Button("Masuk");
    private final ProgressBar progress = AppDialog.newProgressBar();

    public LoginView(AuthUseCase authUseCase, IUserRepository userRepository) {
        this.authUseCase = authUseCase;
        this.userRepository = userRepository;

        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        addClassName("auth-view");

        username.setWidthFull();
        username.setAutofocus(true);
        password.setWidthFull();
        NativeIds.assignToInput(username, "login-username");
        NativeIds.assignToInput(password, "login-password");

        submit.setId("login-submit");
        submit.setWidthFull();
        submit.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submit.addClickShortcut(Key.ENTER);
        submit.addClickListener(e -> login());

        PageCard card = new PageCard(new H2("Cashflow"), new Paragraph("Masuk untuk mencatat keuangan Anda."),
                username, password, progress, submit, new RouterLink("Belum punya akun? Daftar", RegisterView.class));
        card.addClassName("auth-card");
        add(card);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (AuthSession.currentUser(userRepository) != null) {
            event.forwardTo(FinanceView.class);
        }
    }

    private void login() {
        progress.setVisible(true);
        submit.setEnabled(false);
        try {
            User user = authUseCase.login(username.getValue(), password.getValue());
            AuthSession.login(user);
            UI.getCurrent().navigate(FinanceView.class);
        } catch (DomainException ex) {
            Notify.error(ex.getMessage());
        } finally {
            progress.setVisible(false);
            submit.setEnabled(true);
        }
    }
}
