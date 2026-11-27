package org.delcom.ifs25005springbootcashflow.framework.view;

import org.delcom.ifs25005springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25005springbootcashflow.domain.entity.User;
import org.delcom.ifs25005springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25005springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25005springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25005springbootcashflow.usecase.UserUseCase;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.UploadHandler;

/** Profil: ubah nama, password (opsional), dan foto. */
@Route(value = "profile", layout = MainLayout.class)
@PageTitle("Profil | Cashflow")
public class ProfileView extends VerticalLayout {

    private final UserUseCase userUseCase;
    private final IUserRepository userRepository;
    private final FileStorage storage;

    private final Avatar avatar = new Avatar();
    private final TextField username = new TextField("Username");
    private final TextField fullName = new TextField("Nama lengkap");
    private final PasswordField password = new PasswordField("Password baru (kosongkan bila tidak diubah)");
    private final ProgressBar progress = AppDialog.newProgressBar();
    private final Button save = new Button("Simpan");

    private byte[] photoBytes;
    private String photoName;

    public ProfileView(UserUseCase userUseCase, IUserRepository userRepository, FileStorage storage) {
        this.userUseCase = userUseCase;
        this.userRepository = userRepository;
        this.storage = storage;
        setPadding(true);

        avatar.setHeight("96px");
        avatar.setWidth("96px");
        username.setReadOnly(true);
        username.setWidthFull();
        fullName.setWidthFull();
        password.setWidthFull();

        Upload upload = new Upload(UploadHandler.inMemory((metadata, data) -> {
            photoName = metadata.fileName();
            photoBytes = data;
        }));
        upload.setAcceptedFileTypes("image/png", "image/jpeg", "image/gif", "image/webp");
        upload.setMaxFiles(1);
        upload.setMaxFileSize((int) FileStorage.MAX_BYTES);
        upload.setUploadButton(new Button("Pilih foto profil"));
        upload.getElement().addEventListener("file-remove", e -> {
            photoBytes = null;
            photoName = null;
        });

        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.addClickListener(e -> save());

        PageCard card = new PageCard(new H2("Profil"), avatar, new Span("Foto profil (maks 5 MB)"), upload,
                username, fullName, password, progress, save);
        card.addClassName("profile-card");
        add(card);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        User user = AuthSession.currentUser(userRepository);
        if (user == null) {
            return;
        }
        username.setValue(user.getUsername());
        fullName.setValue(user.getFullName());
        avatar.setName(user.getFullName());
        avatar.setImage(user.hasPhoto() ? storage.url(user.getPhotoPath()) : null);
    }

    private void save() {
        User user = AuthSession.currentUser(userRepository);
        if (user == null) {
            return;
        }
        progress.setVisible(true);
        save.setEnabled(false);
        try {
            userUseCase.updateProfile(user.getId(), fullName.getValue(), password.getValue());
            if (photoBytes != null) {
                userUseCase.updatePhoto(user.getId(), storage.store(photoName, photoBytes));
            }
            Notify.success("Profil berhasil disimpan");
            // Muat ulang halaman agar avatar di sidebar ikut berubah.
            getUI().ifPresent(ui -> ui.getPage().reload());
        } catch (DomainException ex) {
            Notify.error(ex.getMessage());
        } finally {
            progress.setVisible(false);
            save.setEnabled(true);
        }
    }
}
