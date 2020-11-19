package com.example.application.views.masterdetailviewjava;

import com.example.application.data.dto.UserDTO;
import com.example.application.data.service.UserService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import elemental.json.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.util.UriUtils;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

@Route(value = "master-detail-view-java")
@PageTitle("master-detail-view-java")
@CssImport("./views/masterdetailviewjava/masterdetailviewjava-view.css")
@RouteAlias(value = "")
public class MasterdetailviewjavaView extends Div {

    private Grid<UserDTO> grid = new Grid<>(UserDTO.class, false);

    private Upload profilePicture;
    private Image profilePicturePreview;
    private TextField email;
    private PasswordField newPassword;

    private Button cancel = new Button("Cancel");
    private Button save = new Button("Save");

    private Binder<UserDTO> binder;

    private UserDTO user;

    public MasterdetailviewjavaView(@Autowired UserService userService) {
        setId("masterdetailviewjava-view");
        // Create UI
        SplitLayout splitLayout = new SplitLayout();
        splitLayout.setSizeFull();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        TemplateRenderer<UserDTO> profilePictureRenderer = TemplateRenderer.<UserDTO>of(
                "<span style='border-radius: 50%; overflow: hidden; display: flex; align-items: center; justify-content: center; width: 64px; height: 64px'><img style='max-width: 100%' src='[[item.profilePicture]]' /></span>")
                .withProperty("profilePicture", UserDTO::getProfilePicture);
        grid.addColumn(profilePictureRenderer).setHeader("Profile Picture").setWidth("96px").setFlexGrow(0);

        grid.addColumn("email").setAutoWidth(true);
        grid.addColumn("newPassword").setAutoWidth(true);
        //grid.setDataProvider(new CrudServiceDataProvider<User, Void>(userService));
        grid.setItems(userService.findUsers());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setHeightFull();

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                Optional<UserDTO> userFromBackend = userService.getDto(event.getValue().getId());
                // when a row is selected but the data is no longer available, refresh grid
                if (userFromBackend.isPresent()) {
                    populateForm(userFromBackend.get());
                } else {
                    refreshGrid();
                }
            } else {
                clearForm();
            }
        });

        // Configure Form
        binder = new Binder<>(UserDTO.class);

        // Bind fields. This where you'd define e.g. validation rules

        binder.bindInstanceFields(this);

        attachImageUpload(profilePicture, profilePicturePreview);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.user == null) {
                    this.user = new UserDTO();
                }
                binder.writeBean(this.user);
                this.user.setProfilePicture(profilePicturePreview.getSrc());

                userService.update(this.user);
                clearForm();
                refreshGrid();
                Notification.show("User details stored.");
            } catch (ValidationException validationException) {
                Notification.show("An exception happened while trying to store the user details.");
            }
        });

    }

    private void createEditorLayout(SplitLayout splitLayout) {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setId("editor-layout");

        Div editorDiv = new Div();
        editorDiv.setId("editor");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();
        Label profilePictureLabel = new Label("Profile Picture");
        profilePicturePreview = new Image();
        profilePicturePreview.addClassName("full-width");
        profilePicture = new Upload();
        profilePicture.getElement().appendChild(profilePicturePreview.getElement());
        email = new TextField("Email");
        newPassword = new PasswordField("New password");
        newPassword.setHelperText("Leave empty to keep current password");
        Component[] fields = new Component[] { profilePictureLabel, profilePicture, email, newPassword };

        for (Component field : fields) {
            ((HasStyle) field).addClassName("full-width");
        }
        formLayout.add(fields);
        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);
    }

    private void createButtonLayout(Div editorLayoutDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setId("button-layout");
        buttonLayout.setWidthFull();
        buttonLayout.setSpacing(true);
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save, cancel);
        editorLayoutDiv.add(buttonLayout);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setId("grid-wrapper");
        wrapper.setWidthFull();
        splitLayout.addToPrimary(wrapper);
        wrapper.add(grid);
    }

    private void attachImageUpload(Upload upload, Image preview) {
        ByteArrayOutputStream uploadBuffer = new ByteArrayOutputStream();

        upload.setAcceptedFileTypes("image/*");
        upload.setReceiver((fileName, mimeType) -> {
            return uploadBuffer;
        });
        upload.addSucceededListener(e -> {
            String mimeType = e.getMIMEType();
            String base64ImageData = Base64.getEncoder().encodeToString(uploadBuffer.toByteArray());
            String dataUrl = "data:" + mimeType + ";base64,"
                    + UriUtils.encodeQuery(base64ImageData, StandardCharsets.UTF_8);
            upload.getElement().setPropertyJson("files", Json.createArray());
            preview.setSrc(dataUrl);
            uploadBuffer.reset();
        });
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getDataProvider().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(UserDTO value) {
        this.user = value;
        binder.readBean(this.user);
        if (value == null) {
            this.profilePicturePreview.setSrc("");
        } else {
            this.profilePicturePreview.setSrc(value.getProfilePicture());
        }

    }
}
