package sample;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.URL;
import java.util.Calendar;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Controller implements Initializable {
    SearchBooksInformation searchBooksInformation = new SearchBooksInformation();
    ExecutorService service = null;
    Task<Void> task = null;

    @FXML
    private MenuItem searchWindowsDeleteMenuItem;
    @FXML
    private MenuItem allDeleteMenuItem;
    @FXML
    private MenuItem closeMenuItem;

    @FXML
    private Button searchButton;

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
    private ProgressIndicator searchingWaitProgressIndeterminate;
    @FXML
    private Label numberOfRecordLabel;

    private String information = "";
    private String oldInformation = "";
    private String numberOfRecord = "";

    @FXML
    public void searchBooks() {
        service = Executors.newSingleThreadExecutor();

        task = new Task<Void>() {
            @Override
            public Void call() {
                searchingWaitProgressIndeterminate.setVisible(true);
                setAllItemsNotTouchedDuringSearchDisable(true);
                searchLibraryInformation();
                return null;
            }

            @Override
            protected void succeeded() {
                searchingWaitProgressIndeterminate.setVisible(false);
                setAllItemsNotTouchedDuringSearchDisable(false);
                numberOfRecordLabel.setText(numberOfRecord);
                service.shutdown();
            }

            @Override
            protected void failed() {
                searchingWaitProgressIndeterminate.setVisible(false);
                setAllItemsNotTouchedDuringSearchDisable(false);
                numberOfRecordLabel.setText(numberOfRecord);
                service.shutdown();
                openAlert(searchBooksInformation.getNumberOfRecord());
            }
        };

        // タスクの状態が変化したら出力する
        task.stateProperty().addListener(new ChangeListener<Worker.State>() {
            @Override
            public void changed(ObservableValue<? extends Worker.State> value, Worker.State oldState, Worker.State newState) {
                System.out.println("New: " + newState + " Old: " + oldState);
            }
        });

        // 強制終了時には並列処理も強制終了かけてあげないと裏でずっと動いたままになってしまう
        // これでもタスクは暫く残ったままだが、URL先のデータを取得したくらいの時間が経過すると消える
        task.addEventHandler(WorkerStateEvent.WORKER_STATE_CANCELLED,
                new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent event) {
                        service.shutdown();
                    }
                });

        service.submit(task);
    }

    public void destroy() {
        if(service != null) {
            task.cancel();
        }
    }

    public void searchLibraryInformation() {
        numberOfRecord = "";

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

        if(textAreaMemoryCheckBox.isSelected()) {
            oldInformation = outputRetrievalResultTextArea.getText() + "\n";
        } else {
            oldInformation = "";
        }

        // 最も時間がかかるメソッド、主に国立国会図書館サーチAPIのせいで
        searchBooksInformation.isSearchedFromNationalDietLibrary();

        information = searchBooksInformation.getSearchedInformation();

        outputRetrievalResultTextArea.setText(oldInformation + information + "\n");

        numberOfRecord = searchBooksInformation.getOutputRecordCount() + " (" + searchBooksInformation.getNumberOfRecord() + ") ";
        if(searchBooksInformation.getNumberOfRecord().equals(Integer.toString(searchBooksInformation.getOutputRecordCount()))) {
            numberOfRecord = searchBooksInformation.getNumberOfRecord();
        }

        titleTextField.setPromptText("");
        creatorTextField.setPromptText("");
        isbnTextField.setPromptText("");
        publisherTextField.setPromptText("");
        issuedFromYearTextField.setPromptText("");
        issuedUntilYearTextField.setPromptText("");

        if(Integer.parseInt(searchBooksInformation.getNumberOfRecord()) > searchBooksInformation.getMaximumRecords()) {
            searchBooksInformation.setMaximumRecords(200);
            openAlert(searchBooksInformation.getNumberOfRecord()); // これはtask.failed()を呼び出すきっかけとなる
        }
        searchBooksInformation.setMaximumRecords(200);
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
        alert.setHeaderText("図書情報検索システム");
        String comment = "これは国立国会図書館サーチAPIを経由して、図書情報を検索するシステムです。\n\n";
        comment += "https://github.com/Morichan/SearchingLibraryInformation";
        alert.setContentText(comment);

        alert.showAndWait();
    }

    public void openAlert(String maxNumber) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("デフォルトの最大検索数の上限は500です。");
        alert.setHeaderText("検索結果が最大検索数の上限に達ました。");
        String contentText = "デフォルトの検索数は200ですが、今回の検索数は最大で" + maxNumber + "になる可能性があります。\n";
        contentText += "全ての検索結果を再び検索し直したい場合は OK を押してください。\n";
        contentText += "その場合、検索時間が大幅に掛かる可能性があります。\n";
        contentText += "なお、国立国会図書館サーチAPIの検索負荷回避のための制約により、500件を超える検索結果は取得できません。\n\n";
        contentText += "取り消しを押した場合、再検索は行いません。\n";
        contentText += "検索項目を増やして検索結果を減らしてください。";
        alert.setContentText(contentText);

        Optional<ButtonType> result = alert.showAndWait();
        if(result.get() == ButtonType.OK) {
            searchBooksInformation.setMaximumRecords(Integer.parseInt(maxNumber));
            searchBooks();
        }
    }

    public void setAllItemsNotTouchedDuringSearchDisable(boolean bool) {
        searchButton.setDisable(bool);

        titleTextField.setDisable(bool);
        creatorTextField.setDisable(bool);
        isbnTextField.setDisable(bool);
        publisherTextField.setDisable(bool);
        issuedFromYearTextField.setDisable(bool);
        issuedUntilYearTextField.setDisable(bool);

        closeMenuItem.setDisable(bool);
        searchWindowsDeleteMenuItem.setDisable(bool);
        allDeleteMenuItem.setDisable(bool);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }
}
