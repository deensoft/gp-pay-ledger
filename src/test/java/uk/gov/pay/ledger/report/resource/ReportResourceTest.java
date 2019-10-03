package uk.gov.pay.ledger.report.resource;

import io.dropwizard.testing.junit.ResourceTestRule;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.pay.ledger.exception.BadRequestExceptionMapper;
import uk.gov.pay.ledger.exception.JerseyViolationExceptionMapper;
import uk.gov.pay.ledger.report.entity.PaymentsStatisticsResult;
import uk.gov.pay.ledger.report.params.PaymentsReportParams;
import uk.gov.pay.ledger.report.service.ReportService;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ReportResourceTest {

    private static final ReportService mockReportService = mock(ReportService.class);

    @ClassRule
    public static ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(new ReportResource(mockReportService))
            .addProvider(BadRequestExceptionMapper.class)
            .addProvider(JerseyViolationExceptionMapper.class)
            .build();

    @Test
    public void getPaymentsByState_shouldReturn400IfGatewayAccountIsNotProvided() {
        Response response = resources
                .target("/v1/report/payments_by_state")
                .request()
                .get();

        assertThat(response.getStatus(), is(400));

        Map responseMap = response.readEntity(Map.class);
        assertThat(responseMap.get("message"), is(List.of("Field [account_id] cannot be empty")));
    }

    @Test
    public void getPaymentsByState_shouldReturn200IfGatewayAccountIdIsNotProvidedButNotRequiredFlag() {
        when(mockReportService.getPaymentCountsByState(null, new PaymentsReportParams()))
                .thenReturn(Map.of("blah", 1L));

        Response response = resources
                .target("/v1/report/payments_by_state")
                .queryParam("override_account_id_restriction", true)
                .request()
                .get();

        Assert.assertThat(response.getStatus(), CoreMatchers.is(200));
    }

    @Test
    public void getPaymentsByState_shouldReturn422IfFromDateInvalid() {
        Response response = resources
                .target("/v1/report/payments_by_state")
                .queryParam("account_id", "abc123")
                .queryParam("from_date", "invalid")
                .request()
                .get();

        assertThat(response.getStatus(), is(422));

        Map responseMap = response.readEntity(Map.class);
        assertThat(responseMap.get("message"), is(List.of("Invalid attribute value: from_date. Must be a valid date")));
    }

    @Test
    public void getPaymentsByState_shouldReturn422IfToDateInvalid() {
        Response response = resources
                .target("/v1/report/payments_by_state")
                .queryParam("account_id", "abc123")
                .queryParam("to_date", "invalid")
                .request()
                .get();

        assertThat(response.getStatus(), is(422));

        Map responseMap = response.readEntity(Map.class);
        assertThat(responseMap.get("message"), is(List.of("Invalid attribute value: to_date. Must be a valid date")));
    }

    @Test
    public void getPaymentsStatistics_shouldReturn400IfGatewayAccountIsNotProvided() {
        Response response = resources
                .target("/v1/report/payments")
                .request()
                .get();

        assertThat(response.getStatus(), is(400));

        Map responseMap = response.readEntity(Map.class);
        assertThat(responseMap.get("message"), is(List.of("Field [account_id] cannot be empty")));
    }

    @Test
    public void getPaymentsStatistics_shouldReturn200IfGatewayAccountIdIsNotProvidedButNotRequiredFlag() {
        when(mockReportService.getPaymentsStatistics(null, new PaymentsReportParams()))
                .thenReturn(new PaymentsStatisticsResult(200L, 20000L));

        Response response = resources
                .target("/v1/report/payments")
                .queryParam("override_account_id_restriction", true)
                .request()
                .get();

        Assert.assertThat(response.getStatus(), CoreMatchers.is(200));
    }

    @Test
    public void getPaymentsStatistics_shouldReturn422IfFromDateInvalid() {
        Response response = resources
                .target("/v1/report/payments")
                .queryParam("account_id", "abc123")
                .queryParam("from_date", "invalid")
                .request()
                .get();

        assertThat(response.getStatus(), is(422));

        Map responseMap = response.readEntity(Map.class);
        assertThat(responseMap.get("message"), is(List.of("Invalid attribute value: from_date. Must be a valid date")));
    }

    @Test
    public void getPaymentsStatistics_shouldReturn422IfToDateInvalid() {
        Response response = resources
                .target("/v1/report/payments")
                .queryParam("account_id", "abc123")
                .queryParam("to_date", "invalid")
                .request()
                .get();

        assertThat(response.getStatus(), is(422));

        Map responseMap = response.readEntity(Map.class);
        assertThat(responseMap.get("message"), is(List.of("Invalid attribute value: to_date. Must be a valid date")));
    }
}