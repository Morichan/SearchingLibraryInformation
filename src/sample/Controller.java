package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.Optional;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    SearchBooksInformation searchBooksInformation = new SearchBooksInformation();

    @FXML
    private TextField titleTextField;
    @FXML
    private TextField creatorTextField;
    @FXML
    private TextField isbnTextField;
    @FXML
    private TextField publisherTextField;
    @FXML
    private TextField issuedFromYearTextField;
    @FXML
    private TextField issuedUntilYearTextField;
    @FXML
    private TextArea outputRetrievalResultTextArea;

    @FXML
    private CheckBox textAreaMemoryCheckBox;
    @FXML
    private CheckBox noIsbnBookIsOutputedCheckBox;
    @FXML
    private CheckBox outputTitleCheckBox;
    @FXML
    private CheckBox outputCreatorCheckBox;
    @FXML
    private CheckBox outputIsbnCheckBox;
    @FXML
    private CheckBox outputPriceCheckBox;
    @FXML
    private CheckBox outputPublisherCheckBox;
    @FXML
    private CheckBox outputIssuedCheckBox;
    @FXML
    private CheckBox outputUriFromNdlCheckBox;
    @FXML
    private CheckBox outputDescriptionCheckBox;
    @FXML
    private CheckBox outputUriFromAmazonSearchCheckBox;

    @FXML
    private Label numberOfRecordLabel;

    private String information = "";
    private String oldInformation = "";

    @FXML
    public void searchBooks() {
        searchBooksInformation.setTitle(titleTextField.getText());
        searchBooksInformation.setCreator(creatorTextField.getText());
        searchBooksInformation.setIsbn(isbnTextField.getText());
        searchBooksInformation.setPublisher(publisherTextField.getText());
        searchBooksInformation.setIssuedFromYear(issuedFromYearTextField.getText());
        searchBooksInformation.setIssuedUntilYear(issuedUntilYearTextField.getText());

        searchBooksInformation.setNoIsbnBookIsOutput(noIsbnBookIsOutputedCheckBox.isSelected());
        searchBooksInformation.setAnswerTitle(outputTitleCheckBox.isSelected());
        searchBooksInformation.setAnswerCreator(outputCreatorCheckBox.isSelected());
        searchBooksInformation.setAnswerIsbn(outputIsbnCheckBox.isSelected());
        searchBooksInformation.setAnswerPrice(outputPriceCheckBox.isSelected());
        searchBooksInformation.setAnswerPublisher(outputPublisherCheckBox.isSelected());
        searchBooksInformation.setAnswerIssued(outputIssuedCheckBox.isSelected());
        searchBooksInformation.setAnswerUriFromNdl(outputUriFromNdlCheckBox.isSelected());
        searchBooksInformation.setAnswerDescription(outputDescriptionCheckBox.isSelected());
        searchBooksInformation.setAnswerUriFromAmazonSearch(outputUriFromAmazonSearchCheckBox.isSelected());

        searchBooksInformation.isSearchedFromNationalDietLibrary();

        information = searchBooksInformation.getSearchedInformation();

        if(textAreaMemoryCheckBox.isSelected()) {
            oldInformation = outputRetrievalResultTextArea.getText() + "\n";
        } else {
            oldInformation = "";
        }

        outputRetrievalResultTextArea.setText(oldInformation + information + "\n");

        String numberOfRecord = searchBooksInformation.getOutputRecordCount() + " (" + searchBooksInformation.getNumberOfRecord() + ")";
        if(searchBooksInformation.getNumberOfRecord().equals(Integer.toString(searchBooksInformation.getOutputRecordCount()))) {
            numberOfRecord = searchBooksInformation.getNumberOfRecord();
        }
        numberOfRecordLabel.setText(numberOfRecord);
        if(Integer.parseInt(searchBooksInformation.getNumberOfRecord()) > searchBooksInformation.getMaximumRecords()) {
            openAlert(searchBooksInformation.getNumberOfRecord());
        }

        if(searchBooksInformation.getMaximumRecords() != 200) {
            searchBooksInformation.setMaximumRecords(200);
        }
        titleTextField.setPromptText("");
        creatorTextField.setPromptText("");
        isbnTextField.setPromptText("");
        publisherTextField.setPromptText("");
        issuedFromYearTextField.setPromptText("");
        issuedUntilYearTextField.setPromptText("");
    }

    @FXML
    public void closeSystemOnFileMenu() {
        System.exit(0);
    }

    @FXML
    public void deleteRetrievalResultTextAreaOnEditMenu() {
        outputRetrievalResultTextArea.setText("");
    }

    @FXML
    public void deleteSearchWindowsTextFieldOnEditMenu() {
        titleTextField.setText("");
        creatorTextField.setText("");
        isbnTextField.setText("");
        publisherTextField.setText("");
        issuedFromYearTextField.setText("");
        issuedUntilYearTextField.setText("");
    }
    @FXML
    public void deleteAllTextAreaAndTextFieldOnEditMenu() {
        deleteRetrievalResultTextAreaOnEditMenu();
        deleteSearchWindowsTextFieldOnEditMenu();
    }

    @FXML
    public void aboutSearchingLibraryInformationOnHelpMenu() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("これは何？");
        alert.setHeaderText("図書検索システム");
        String comment = "これは国立国会図書館のAPIを経由して、図書情報を出力するシステムです。\n\n";
        comment += "https://github.com/Morichan/SearchingLibraryInformation";
        alert.setContentText(comment);

        alert.showAndWait();
    }

    public void openAlert(String maxNumber) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setWidth(600);
        alert.setTitle("デフォルトの最大検索数の上限は200です。");
        alert.setHeaderText("検索結果が最大検索数の上限に達ました。");
        String contentText = "デフォルトの最大検索数は200ですが、今回の検索数は最大で" + maxNumber + "になる可能性があります。\n";
        contentText += "全ての検索結果を再び検索し直したい場合は OK を押してください。\n";
        contentText += "なお、検索時間が大幅に掛かる可能性があります。\n\n";
        contentText += "取り消しを押した場合、再検索は行いません。\n";
        contentText += "検索項目を増やして検索結果を減らしてください。";
        alert.setContentText(contentText);

        Optional<ButtonType> result = alert.showAndWait();
        if(result.get() == ButtonType.OK) {
            searchBooksInformation.setMaximumRecords(Integer.parseInt(maxNumber));
            searchBooks();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }
}
