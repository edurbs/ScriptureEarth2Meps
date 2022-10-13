package com.scriptureearth2meps.application.views;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import org.apache.commons.io.FileUtils;

import com.scriptureearth2meps.control.BibleSetup;
import com.scriptureearth2meps.model.Bible;
import com.scriptureearth2meps.model.Language;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;

@PageTitle("ScriptureEarth to Meps format")
@Route(value = "/se2meps")
public class WebView extends HorizontalLayout {

	// private TextField name;
	// private Button sayHello;

	private static final long serialVersionUID = 4118526693136198460L;
	private BibleSetup bibleSetup;
	private ComboBox<Bible> comboBibles = new ComboBox<>();;
	private ComboBox<Language> comboLanguages = new ComboBox<>();
	private TextField wordSee = new TextField();
	private TextField glotal = new TextField();
	private Div div = new Div();
	private Div progressBarLabel = new Div();
	private Div progressBarSubLabel = new Div();
	private ProgressBar progressBar = new ProgressBar();
	
	private Button cancelButton = new Button("Cancel formatting");
	private Notification notification = new Notification();
	private TextArea textArea = new TextArea("Instruction",
			"Download the zip file and start with the Step 3 of PPD: Convert Bible Documents to MEPS Open each html file with Microsoft Word and save it as Meps Listing file .mps");
	
	private Div divDownload = new Div();
	private Button buttonStart = new Button("Start");
	private FormLayout formLayout = new FormLayout();

	public WebView() {

		try {
			this.bibleSetup = new BibleSetup();
		} catch (Exception e) {
			e.printStackTrace();
		}

		wordSee.setLabel("Word \"See\" translation:");
		wordSee.setHelperText("Enter the translation of the word \"See\" in imperative form");

		glotal.setLabel("Glotal character:");
		glotal.setHelperText("Enter the glottal character used by the language");

		Collection<Language> languageList = bibleSetup.getLanguageList();

		comboLanguages.setLabel("Select a language");
		comboLanguages.setItems(languageList);
		comboLanguages.setItemLabelGenerator(Language::getLanguage);
		comboLanguages.setPlaceholder("No language selected");

		comboLanguages.addValueChangeListener(e -> {
			try {
				showComboBible();
			} catch (Exception e1) {
				showError(e1);
			}
		});

		comboBibles.setLabel("Select a Bible");
		comboBibles.setVisible(false);

		
		formLayout.add(wordSee, glotal, comboLanguages, comboBibles);
		formLayout.setResponsiveSteps(new ResponsiveStep("0", 1), new ResponsiveStep("500px", 2));
		formLayout.setSizeFull();

		div.add(formLayout);
		

		buttonStart.addClickListener(e -> {
			try {
				onClick();
			} catch (Exception e1) {
				showError(e1);
			}
		});
		buttonStart.setVisible(true);
		div.add(buttonStart);

		add(div);

		progressBar.setMin(0);
		progressBar.setMax(100);
		progressBar.setValue(0);
		

		progressBar.setVisible(false);
		cancelButton.setVisible(false);
		progressBarLabel.setVisible(false);
		progressBarSubLabel.setVisible(false);

		progressBarSubLabel.getStyle().set("font-size", "var(--lumo-font-size-xs)");
		progressBarSubLabel.setText("This process can take some minutes. Do not close this Window.");

		div.add(progressBarLabel, progressBar, progressBarSubLabel, cancelButton);

		divDownload.add(textArea);
		//divDownload.add(buttonDownload);

		div.add(divDownload);
		divDownload.setVisible(false);

		add(div);		

		setSizeFull();
		setHeightFull();
		setAlignItems(Alignment.START);

		setSpacing(true);

		setMargin(true);

	}

	private void onClick() throws Exception {
		divDownload.setVisible(false);
		if (!wordSee.getValue().isBlank() && !glotal.getValue().isBlank() && comboLanguages.getValue() != null) {
			
			if (!bibleSetup.verifyWebBible()) {
				showMessage("This language " + bibleSetup.getLanguageCode()
						+ " does not have the Bible web page. Choose other.");
				return;
			} 
				

			if (comboBibles.getValue() == null && bibleSetup.hasMoreBibles()) {
				showMessage("This language " + bibleSetup.getLanguageCode()
				+ " have more than one Bible. Choose one.");
				return;
			}
			if (comboBibles.getValue() != null && bibleSetup.hasMoreBibles()) {
				bibleSetup.setBibleCode(comboBibles.getValue().getBible());
			}
			if (bibleSetup.getBibleCode() == null && !bibleSetup.hasMoreBibles()) {
				bibleSetup.setBibleCode(bibleSetup.getLanguageCode());
			}

			bibleSetup.setWordSee(wordSee.getValue());
			bibleSetup.setGlotal(glotal.getValue());
			bibleSetup.setLanguageCode(comboLanguages.getValue().getLanguage());

			progressBar.setVisible(true);
			progressBarLabel.setVisible(true);
			progressBarSubLabel.setVisible(true);
			cancelButton.setVisible(true);

			progressBarLabel.setText("Formating... please wait some minutes");

			bibleSetup.process(this::processingUpdated, this::processingSucceeded);

			cancelButton.addClickListener(e -> {
				bibleSetup.setShouldStop(true);

				showMessage("Cancelled!");
			});

		} else {
			showMessage("Fill in all form fields!");
		}
	}

	private void processingUpdated(Float percent) {
		// use access when modifying the UI from a background thread
		this.getUI().orElseThrow().access(() -> {
			progressBarSubLabel.setText(bibleSetup.getChapterUrl());
			progressBar.setValue(percent);
		});

	}

	private void processingSucceeded() {
		this.getUI().orElseThrow().access(() -> {
			progressBar.setVisible(false);
			progressBarLabel.setVisible(false);
			progressBarSubLabel.setVisible(false);
			cancelButton.setVisible(false);
			showMessage("Done!");

			if (bibleSetup.getOutputZipFileName() != null) {

				File file = new File(bibleSetup.getOutputZipFileName());

				Button buttonDownload = new Button("Download formatted files", event -> {

					final StreamResource resource = new StreamResource(file.getName(), () -> {
						try {
							return new ByteArrayInputStream(FileUtils.readFileToByteArray(file));
						} catch (IOException e) {
							showError(e);
						}
						return null;
					});
					final StreamRegistration registration = VaadinSession.getCurrent().getResourceRegistry()
							.registerResource(resource);
					UI.getCurrent().getPage().open(registration.getResourceUri().toString());

				});
				
				divDownload.add(buttonDownload);
				divDownload.setVisible(true);

			}

		});
	}

	private void showComboBible() throws IOException {
		comboBibles.setValue(null);
		if (comboLanguages.getValue() != null) {
			if (!comboLanguages.getValue().getLanguage().equals(bibleSetup.getLanguageCode())) {
				bibleSetup.setLanguageCode(comboLanguages.getValue().getLanguage());
				if (!bibleSetup.verifyWebBible()) {
					showMessage("This language " + bibleSetup.getLanguageCode()
							+ " does not have the Bible web page. Choose other.");
				} else {
					if (bibleSetup.hasMoreBibles()) {
						showMessage("This language " + bibleSetup.getLanguageCode()
								+ " have more than one Bible. Choose one.");
						Collection<Bible> bibleList = bibleSetup.getBibleList();
						comboBibles.setItems(bibleList);
						comboBibles.setItemLabelGenerator(Bible::getBible);
						comboBibles.setPlaceholder("No language selected");
						comboBibles.setVisible(true);
					} else {
						bibleSetup.setBibleCode(bibleSetup.getLanguageCode());
						comboBibles.setVisible(false);
					}
				}
			}
		}
	}

	private void showMessage(String msg) {

		notification.setDuration(3000);
		notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
		notification.setPosition(Position.TOP_END);
		notification.setText(msg);
		notification.open();

	}

	private void showError(Exception e) {
		e.printStackTrace();
	}

}
