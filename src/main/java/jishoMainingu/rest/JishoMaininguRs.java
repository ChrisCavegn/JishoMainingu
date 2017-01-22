package jishoMainingu.rest;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.springframework.stereotype.Component;

import jishoMainingu.backend.jisho.JishoAccess;
import jishoMainingu.backend.jisho.model.DataDto;
import jishoMainingu.function.excel.ExcelWriter;
import jishoMainingu.function.logging.Logging;
import jishoMainingu.function.specification.DataSpecification;
import jishoMainingu.function.specification.SpecificationCalculator;

@Component
@Path("/")
public class JishoMaininguRs {

	@Inject
	private JishoAccess jishoAccess;

	@Inject
	private SpecificationCalculator calculator;

	@Inject
	private ExcelWriter excelWriter;

	@GET
	public String useage() {
		StringBuilder message = new StringBuilder();
		message.append("<html>");
		message.append("<body>");
		message.append(
				"Anzeige von JSON: <a target=\"_blank\" href=\"http://localhost:8080/json?keyword=jlpt-n3\">http://localhost:8080/json?keyword=jlpt-n3</a>");
		message.append("<p/>");
		message.append(
				"Export nach Excel:  <a target=\"_blank\" href=\"http://localhost:8080/excel?keyword=jlpt-n3\">http://localhost:8080/excel?keyword=jlpt-n3</a>");
		message.append("<p/>");
		message.append(
				"Export nach Excel:  <a target=\"_blank\" href=\"http://localhost:8080/excel?keyword=jlpt-n3&maxPage=2\">http://localhost:8080/excel?keyword=jlpt-n3&maxPage=2</a>");
		message.append("<p/>");
		message.append("<p/>");
		message.append("Anstelle von keyword kannst du natürlich auch nach anderen Dingen suchen ...");
		message.append("</body>");
		message.append("</html>");
		return message.toString();
	}

	@GET
	@Path("json")
	@Produces("application/json")
	public List<DataDto> raw(@QueryParam("keyword") String keyword, @QueryParam("maxPage") Integer maxPage) {
		Logging logging = new Logging();
		logging.createEntry(String.format(" GET [excel] keyword=%s", keyword));

		List<DataDto> data = jishoAccess.read(keyword, maxPage, logging);

		return data;
	}

	@GET
	@Path("excel")
	@Produces("application/vnd.ms-excel")
	public Response abc(@QueryParam("keyword") String keyword, @QueryParam("maxPage") Integer maxPage) {

		Logging logging = new Logging();
		logging.createEntry(String.format(" GET [excel] keyword=%s", keyword));

		List<DataDto> data = jishoAccess.read(keyword, maxPage, logging);

		DataSpecification specification = calculator.calculate(data, logging);

		try {
			ByteArrayOutputStream outputStream = excelWriter.createWorkbook(keyword, data, specification, logging);

			ResponseBuilder responseBuilder = Response.ok(outputStream.toByteArray());

			return responseBuilder
					.header("Content-Disposition", "inline; filename=jisho-translations-" + keyword + ".xls").build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.serverError().build();
		}
	}
}
