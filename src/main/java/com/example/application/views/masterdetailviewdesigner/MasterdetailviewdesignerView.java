package com.example.application.views.masterdetailviewdesigner;

import java.util.Optional;

import com.example.application.data.entity.User;
import com.example.application.data.service.UserService;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.polymertemplate.Id;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.templatemodel.TemplateModel;

import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.artur.helpers.CrudServiceDataProvider;

import com.vaadin.flow.data.converter.StringToIntegerConverter;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import java.io.ByteArrayOutputStream;
import com.vaadin.flow.component.upload.Upload;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import com.vaadin.flow.component.Component;
import org.springframework.web.util.UriUtils;
import elemental.json.Json;
import com.vaadin.flow.data.converter.StringToDoubleConverter;

@Route(value = "master-detail-view-designer")
@PageTitle("master-detail-view-designer")
@JsModule("./views/masterdetailviewdesigner/masterdetailviewdesigner-view.js")
@Tag("masterdetailviewdesigner-view")
public class MasterdetailviewdesignerView extends PolymerTemplate<TemplateModel> {

    // This is the Java companion file of a design
    // You can find the design file in
    // /frontend/src/views/views/masterdetailviewdesigner/masterdetailviewdesigner-view.js
    // The design can be easily edited by using Vaadin Designer
    // (vaadin.com/designer)

    // Grid is created here and not mapped from the template so we can pass the
    // class to the constructor
    private Grid<User> grid = new Grid<>(User.class, false);

    @Id
    private Upload profilePicture;
    @Id
    private Image profilePicturePreview;
    @Id
    private TextField email;
    @Id
    private PasswordField password;

    @Id
    private Button cancel;
    @Id
    private Button save;

    private Binder<User> binder;

    private User user;

    public MasterdetailviewdesignerView(@Autowired UserService userService) {
        setId("masterdetailviewdesigner-view");

        TemplateRenderer<User> profilePictureRenderer = TemplateRenderer.<User>of(
                "<span style='border-radius: 50%; overflow: hidden; display: flex; align-items: center; justify-content: center; width: 64px; height: 64px'><img style='max-width: 100%' src='[[item.profilePicture]]' /></span>")
                .withProperty("profilePicture", User::getProfilePicture);
        grid.addColumn(profilePictureRenderer).setHeader("Profile Picture").setWidth("96px").setFlexGrow(0);

        grid.addColumn("email").setAutoWidth(true);
        grid.addColumn("password").setAutoWidth(true);
        grid.setDataProvider(new CrudServiceDataProvider<User, Void>(userService));
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setHeightFull();
        // Add to the `<slot name="grid">` defined in the template
        grid.getElement().setAttribute("slot", "grid");
        getElement().appendChild(grid.getElement());

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                Optional<User> userFromBackend = userService.get(event.getValue().getId());
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
        binder = new Binder<>(User.class);

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
                    this.user = new User();
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

    private void populateForm(User value) {
        this.user = value;
        binder.readBean(this.user);
        if (value == null) {
            this.profilePicturePreview.setSrc("");
        } else {
            this.profilePicturePreview.setSrc(value.getProfilePicture());
        }

    }
}
