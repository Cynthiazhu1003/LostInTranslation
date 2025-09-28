package translation;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GUI {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JSONTranslator jsonTranslator = new JSONTranslator();
            CountryCodeConverter countryConverter = new CountryCodeConverter();
            LanguageCodeConverter languageConverter = new LanguageCodeConverter();

            List<String> languageNames = new ArrayList<>();
            for (String langCode : jsonTranslator.getLanguageCodes()) {
                String name = languageConverter.fromLanguageCode(langCode);
                languageNames.add(name != null ? name : langCode);
            }
            Collections.sort(languageNames); // Sorted might be better?

            List<String> countryNames = new ArrayList<>();
            for (String countryCode : jsonTranslator.getCountryCodes()) {
                String name = countryConverter.fromCountryCode(countryCode);
                countryNames.add(name != null ? name : countryCode);
            }
            // countryNames should be already sorted since sample is in alphabetical order

            JFrame frame = new JFrame("Country Name Translator");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            JPanel topPanel = new JPanel();
            topPanel.add(new JLabel("Language:"));
            JComboBox<String> languageBox = new JComboBox<>(languageNames.toArray(new String[0]));

            int defaultIndex = -1;
            for (int i = 0; i < languageBox.getItemCount(); i++) {
                if ("German".equalsIgnoreCase(languageBox.getItemAt(i))) { // Consistent since it's default in gif
                    defaultIndex = i;
                    break;
                }
            }
            if (defaultIndex >= 0) languageBox.setSelectedIndex(defaultIndex); // Delete this part if you guys don't want a default selection

            topPanel.add(languageBox);

            JPanel centerPanel = new JPanel();
            centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

            JPanel translationRow = new JPanel();
            JLabel translationLabel = new JLabel("Translation:");
            JLabel translatedName = new JLabel(" ");// Bug: Korean and Thai translations are boxes (font doesn't support, I didn't find any font that supports all languages at the same time)
            translationRow.add(translationLabel);
            translationRow.add(translatedName);
            centerPanel.add(translationRow);

            JList<String> countryList = new JList<>(countryNames.toArray(new String[0]));
            countryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            countryList.setVisibleRowCount(10);
            countryList.setFixedCellWidth(200);
            countryList.setAlignmentX(Component.CENTER_ALIGNMENT);
            JScrollPane listScroll = new JScrollPane(countryList);
            listScroll.setAlignmentX(Component.CENTER_ALIGNMENT);
            centerPanel.add(listScroll);

            countryList.addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    updateTranslation(jsonTranslator, countryConverter, languageConverter,
                            countryList, languageBox, translatedName);
                }
            });

            languageBox.addActionListener(e ->
                    updateTranslation(jsonTranslator, countryConverter, languageConverter,
                            countryList, languageBox, translatedName)
            );

            frame.getContentPane().setLayout(new BorderLayout(8, 8));
            frame.getContentPane().add(topPanel, BorderLayout.NORTH);
            frame.getContentPane().add(centerPanel, BorderLayout.CENTER);

            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    // Helper to change the text via listeners
    private static void updateTranslation(JSONTranslator translator,
                                          CountryCodeConverter countryConverter,
                                          LanguageCodeConverter languageConverter,
                                          JList<String> countryList,
                                          JComboBox<String> languageBox,
                                          JLabel translatedName) {
        String selectedCountryName = countryList.getSelectedValue();
        String selectedLanguageName = (String) languageBox.getSelectedItem();
        if (selectedCountryName == null || selectedLanguageName == null) {
            translatedName.setText(" ");
            return;
        }

        String countryCode = countryConverter.fromCountry(selectedCountryName);
        String languageCode = languageConverter.fromLanguage(selectedLanguageName);

        if (countryCode == null || languageCode == null) {
            translatedName.setText("(no translation found)");
            return;
        }

        String result = translator.translate(countryCode.toLowerCase(), languageCode.toLowerCase());
        translatedName.setText(result == null ? "(no translation found)" : result);
    }
}