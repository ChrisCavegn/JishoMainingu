package jishoMainingu.rest;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
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
import jishoMainingu.function.excel.model.ExcelEntry;
import jishoMainingu.function.filter.ContentFilter;
import jishoMainingu.function.logging.Logging;
import jishoMainingu.function.modeladaption.ModelConverter;
import jishoMainingu.function.modeladaption.PartsOfSpeechTransformer;
import jishoMainingu.function.specification.DataSpecification;
import jishoMainingu.function.specification.SpecificationCalculator;

@Component
@Path("/")
public class JishoMaininguRs {

	@Inject
	private JishoAccess jishoAccess;

	@Inject
	private ContentFilter contentFilter;

	@Inject
	private SpecificationCalculator calculator;

	@Inject
	private ModelConverter modelConverter;

	@Inject
	private PartsOfSpeechTransformer posTransformer;

	@Inject
	private ExcelWriter excelWriter;

	@GET
	public String useage() {
		String baseUrl = "http://localhost:8080/";

		StringBuilder message = new StringBuilder();
		message.append("<html>");
		message.append("<body>");
		message.append("Anzeige von POS-Mapping: <p/>");
		message.append(getHref(baseUrl, "posMapping")).append("<p/>");
		message.append("<p/>");
		message.append("Anzeige von JSON: <p/>");
		message.append(getHref(baseUrl, "json?keyword=jlpt-n3")).append("<p/>");
		message.append(getHref(baseUrl, "json?keyword=jlpt-n3&maxPage=2")).append("<p/>");
		message.append(getHref(baseUrl, "json?keyword=jlpt-n3&filterEvilSources=false&maxPage=2")).append("<p/>");
		message.append("<p/>");
		message.append("Export nach Excel: <p/>");
		message.append(getHref(baseUrl, "excel?keyword=jlpt-n3")).append("<p/>");
		message.append(getHref(baseUrl, "excel?keyword=jlpt-n3&maxPage=2")).append("<p/>");
		message.append(getHref(baseUrl, "excel?keyword=jlpt-n3&filterEvilSources=false&maxPage=2")).append("<p/>");
		message.append("<p/>");
		message.append("Anstelle von keyword kannst du natürlich auch nach anderen Dingen suchen ...");
		message.append("</body>");
		message.append("</html>");
		return message.toString();
	}

	private String getHref(String url, String urn) {
		return String.format("<a target=\"_blank\" href=\"%s%s\">%s%s</a>", url, urn, url, urn);
	}

	@GET
	@Path("posMapping")
	@Produces("application/json")
	public Map<String, String> getAdverbMapping() {
		Logging logging = new Logging();

		posTransformer.initialize(logging);

		return posTransformer.getMapping();
	}

	@GET
	@Path("json")
	@Produces("application/json")
	public List<DataDto> json(@QueryParam("keyword") String keyword,
			@QueryParam("filterEvilSources") @DefaultValue(value = "true") boolean filterEvilSources,
			@QueryParam("maxPage") Integer maxPage) {
		Logging logging = new Logging();
		logging.createEntry(String.format(" GET [excel] keyword=%s", keyword));

		List<DataDto> data = jishoAccess.read(keyword, maxPage, logging);

		contentFilter.filter(data, filterEvilSources, logging);

		return data;
	}

	@GET
	@Path("excel")
	@Produces("application/vnd.ms-excel")
	public Response abc(@QueryParam("keyword") String keyword,
			@QueryParam("filterEvilSources") @DefaultValue(value = "true") boolean filterEvilSources,
			@QueryParam("maxPage") Integer maxPage) {

		Logging logging = new Logging();
		logging.createEntry(String.format(" GET [excel] keyword=%s", keyword));

		List<DataDto> data = jishoAccess.read(keyword, maxPage, logging);

		DataSpecification specification = calculator.calculate(data, logging);

		contentFilter.filter(data, filterEvilSources, logging);

		List<ExcelEntry> excelData = modelConverter.convert(data, logging);

		posTransformer.initialize(logging);
		posTransformer.createModifiedPOS(excelData);
		
		try {
			ByteArrayOutputStream outputStream = excelWriter.createWorkbook(keyword, excelData, specification, logging);

			ResponseBuilder responseBuilder = Response.ok(outputStream.toByteArray());

			return responseBuilder
					.header("Content-Disposition", "inline; filename=jisho-translations-" + keyword + ".xls").build();
		}
		catch (Exception e) {
			e.printStackTrace();
			return Response.serverError().build();
		}
	}
}
