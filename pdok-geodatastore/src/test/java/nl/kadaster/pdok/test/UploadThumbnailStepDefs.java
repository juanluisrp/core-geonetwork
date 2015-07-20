package nl.kadaster.pdok.test;
import cucumber.api.PendingException;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.AbstractCoreIntegrationTest;
import org.junit.Test;

import java.io.InputStream;

/**
 * User: bloemj
 * Date: 17-7-2015
 * Time: 10:58
 */
public class UploadThumbnailStepDefs extends  AbstractCoreIntegrationTest {

    static InputStream dataset;
    static InputStream icon;
    static String doc;
    @Given("^a file stream containing the bytes from \"(.*?)\", an icon stream containing the \"(.*?)\" and the following metadata\\.$")
    public void a_file_stream_containing_the_bytes_from_an_icon_stream_containing_the_and_the_following_metadata(String arg1, String arg2, String arg3) throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        dataset = ClassLoader.getSystemResourceAsStream(arg1);
        icon = ClassLoader.getSystemResourceAsStream(arg2);
        doc = arg3;
    }

    @When("^the data is uploaded through the API\\.$")
    public void the_data_is_uploaded_through_the_API() throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("^the dataset is published\\.$")
    public void the_dataset_is_published() throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @And("^the metadata is valid.$")
    public void the_metadata_is_valid() throws Throwable {
        // Express the Regexp above with the code you wish you had
        throw new PendingException();
    }

    @And("^there is a usersession.$")
    public void there_is_a_usersession() throws Throwable {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

    }
}
