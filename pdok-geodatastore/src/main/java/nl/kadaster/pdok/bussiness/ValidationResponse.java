package nl.kadaster.pdok.bussiness;

import java.util.List;

/**
 * Created by juanluisrp on 10/11/2015.
 */
public class ValidationResponse {
    private String status;
    private List<String> errorMessageList;

    public List<String> getErrorMessageList() {
        return errorMessageList;
    }

    public void setErrorMessageList(List<String> errorMessageList) {
        this.errorMessageList = errorMessageList;
    }

    public String getStatus() {

        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
