package com.scriptureearth2meps.application.views;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import org.springframework.util.concurrent.ListenableFuture;

import com.scriptureearth2meps.control.BibleSetup;
import com.scriptureearth2meps.model.Bible;
import com.scriptureearth2meps.model.Language;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("ScriptureEarth to Meps format")
@Route(value = "/se2meps")
public class WebView extends HorizontalLayout {

	// private TextField name;
	// private Button sayHello;

	/**
	 * 
	 */
	private static final long serialVersionUID = 2639351443236902882L;

	private BibleSetup bibleSetup;
	private ComboBox<Bible> comboBibles = new ComboBox<>();;
	private ComboBox<Language> comboLanguages = new ComboBox<>();
	private TextField wordSee = new TextField();
	private TextField glotal = new TextField();
	private Div div = new Div();
	private Div progressBarLabel = new Div();
	private Div progressBarSubLabel = new Div();
	private ProgressBar progressBar = new ProgressBar();
	private Button button;
	private Button cancelButton = new Button("Cancel formatting");
	Notification notification = new Notification();

	public WebView() {

		try {
			this.bibleSetup = new BibleSetup();
		} catch (Exception e) {
			e.printStackTrace();
		}

		wordSee.setLabel("Word \"See\" translation:");
		wordSee.setHelperText("Enter the translation of the word \"See\" in imperative form");

		glotal.setLabel("Glotal character:");
		glotal.setHelperText("Enter the glotal character used byt the language");

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

		FormLayout formLayout = new FormLayout();
		formLayout.add(wordSee, glotal, comboLanguages, comboBibles);
		formLayout.setResponsiveSteps(new ResponsiveStep("0", 1), new ResponsiveStep("500px", 2));

		div.add(formLayout);

		button = new Button("Start");

		button.addClickListener(e -> {
			try {
				onClick();
			} catch (Exception e1) {
				showError(e1);
			}
		});
		button.setVisible(true);
		div.add(button);

		// HorizontalLayout horizontalLayout = new HorizontalLayout();
		// horizontalLayout.setMargin(true);
		setMargin(true);
		add(div);

		progressBar.setMin(0);
		progressBar.setMax(100);
		progressBar.setValue(0);

		// progressBar.setIndeterminate(true);

		progressBar.setVisible(false);
		cancelButton.setVisible(false);
		progressBarLabel.setVisible(false);
		progressBarSubLabel.setVisible(false);

		// progressBarLabel.setText("Formating...");

		progressBarSubLabel.getStyle().set("font-size", "var(--lumo-font-size-xs)");
		progressBarSubLabel.setText("This process can take some minutes");

		div.add(progressBarLabel, progressBar, progressBarSubLabel, cancelButton);
		// div.add(progressBar, cancelButton);

		// horizontalLayout.add(div);
		// add(horizontalLayout);
		add(div);

	}

	private void onClick() throws Exception {
		if (!wordSee.getValue().isBlank() && !glotal.getValue().isBlank() && comboLanguages.getValue() != null
				) {

			//&& (comboBibles.getValue() != null || bibleSetup.getBibleCode() != null)
			if (comboBibles.getValue() == null && bibleSetup.hasMoreBibles()) {				
				Notification.show("Choose a Bible");
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
			
			// UI ui = button.getUI().orElseThrow();
			progressBar.setVisible(true);
			progressBarLabel.setVisible(true);
			progressBarSubLabel.setVisible(true);
			cancelButton.setVisible(true);

			progressBarLabel.setText("Formating... please wait some minutes");

			// final ExecutorService executorService = Executors.newSingleThreadExecutor();

			bibleSetup.process(this::processingUpdated, this::processingSucceeded);
			// ListenableFuture<String> future = bibleSetup.process(this::processingUpdated,
			// this::processingSucceeded);

			cancelButton.addClickListener(e -> {
				// bibleSetup.get.cancel(true);
				bibleSetup.setShouldStop(true);

				Notification.show("Cancelled!");
			});

		} else {
			showMessage("Fill the form!");
		}
	}

	private void processingUpdated(Float percent) {
		// use access when modifying the UI from a background thread
		this.getUI().orElseThrow().access(() -> {
			progressBarSubLabel.setText(bibleSetup.getChapterUrl());
			progressBar.setValue(percent);
		});
		// progressBarLabel.setText(string);
		// System.out.println("processingUpdated: " + string);
	}

	private void processingSucceeded() {
		this.getUI().orElseThrow().access(() -> {
			progressBar.setVisible(false);
			progressBarLabel.setVisible(false);
			progressBarSubLabel.setVisible(false);
			cancelButton.setVisible(false);
			Notification.show("Done!");
		});
	}
	/*
	 * private void updateUi(UI ui, String result) { ui.access(() -> {
	 * showMessage(result); System.out.println(result);
	 * 
	 * // progressBar.setVisible(false); // cancelButton.setVisible(false); }); }
	 */

	private void showComboBible() throws IOException {
		if (comboLanguages.getValue() != null) {
			if (!comboLanguages.getValue().getLanguage().equals(bibleSetup.getLanguageCode())) {
				bibleSetup.setLanguageCode(comboLanguages.getValue().getLanguage());
				if (!bibleSetup.verifyWebBible()) {
					showMessage("This language " + bibleSetup.getLanguageCode() + " does not have the Bible web page. Choose other.");
				} else {
					if (bibleSetup.hasMoreBibles()) {
						showMessage("This language " + bibleSetup.getLanguageCode() + " have more than one Bible. Choose one.");
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
		// System.out.println(msg);
		/*
		 * notification.setDuration(3000);
		 * notification.setPosition(Position.BOTTOM_CENTER);
		 * notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
		 * 
		 * Div text = new Div(new Text(msg));
		 * 
		 * Button closeButton = new Button(new Icon("lumo", "cross"));
		 * closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
		 * closeButton.getElement().setAttribute("aria-label", "Close");
		 * closeButton.addClickListener(event -> { notification.close(); });
		 * 
		 * HorizontalLayout layout = new HorizontalLayout(text, closeButton);
		 * layout.setAlignItems(Alignment.CENTER);
		 * 
		 * notification.add(layout); notification.open();
		 */
		Notification.show(msg);
	}

	private void showError(Exception e) {
		e.printStackTrace();
	}

}
