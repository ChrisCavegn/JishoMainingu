package jishoMainingu.rest;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.springframework.stereotype.Component;

import jishoMainingu.backend.jisho.model.DataDto;
import jishoMainingu.function.dataprovider.JishoDataProvider;
import jishoMainingu.function.excel.ExcelWriter;
import jishoMainingu.function.excel.model.ExcelEntry;
import jishoMainingu.function.filter.ContentFilter;
import jishoMainingu.function.logging.LogEntry;
import jishoMainingu.function.logging.Logging;
import jishoMainingu.function.modeladaption.ModelConverter;
import jishoMainingu.function.modeladaption.PartsOfSpeechTransformer;
import jishoMainingu.function.specification.DataSpecification;
import jishoMainingu.function.specification.SpecificationCalculator;
import jishoMainingu.persistence.JishoDataDto;
import jishoMainingu.persistence.SimplifiedJishoDataDto;

@Component
@Path("/")
public class JishoMaininguRs {

	@Inject
	private JishoDataProvider jishoIntegration;

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
		message.append("Anzeige von POS-Mapping:");
		message.append("<ul>");
		message.append(getLi(getHref(baseUrl, "posMapping")));
		message.append("</ul>");
		message.append("<br/>");
		message.append("Anzeige von JSON:");
		message.append("<ul>");
		message.append(getLi(getHref(baseUrl, "json?keyword=jlpt-n3")));
		message.append(getLi(getHref(baseUrl, "json?keyword=jlpt-n3&maxPage=2")));
		message.append(getLi(getHref(baseUrl, "json?keyword=jlpt-n3&filterEvilSources=false&maxPage=2")));
		message.append("</ul>");
		message.append("<br/>");
		message.append("Export nach Excel:");
		message.append("<ul>");
		message.append(getLi(getHref(baseUrl, "excel?keyword=jlpt-n3")));
		message.append(getLi(getHref(baseUrl, "excel?keyword=jlpt-n3&maxPage=2")));
		message.append(getLi(getHref(baseUrl, "excel?keyword=jlpt-n3&filterEvilSources=false&maxPage=2")));
		message.append("</ul>");
		message.append("<br/>");
		message.append("Zwischengespeicherte Daten:");
		message.append("<ul>");
		message.append(getLi(getHref(baseUrl, "cachedData")));
		message.append(getLi(getHref(baseUrl, "resetCache")));
		message.append("</ul>");
		message.append("<br/>");
		message.append("Anstelle von 'jlpt-n3' kann natürlich auch nach anderen keywords gesucht werden ...");
		message.append("</body>");
		message.append("</html>");
		return message.toString();
	}

	private Object getLi(String content) {
		return String.format("<li>%s</li>", content);
	}

	private String getHref(String url, String urn) {
		return String.format("<a target=\"_blank\" href=\"%s%s\">%s%s</a>", url, urn, url, urn);
	}

	@GET
	@Path("posMapping")
	@Produces("application/json")
	public Map<String, String> getPosMapping() {
		Logging logging = new Logging();

		posTransformer.initialize(logging);

		return posTransformer.getMapping();
	}

	@GET
	@Path("json")
	@Produces("application/json")
	public List<DataDto> json(@QueryParam("keyword") String keyword, @QueryParam("filterEvilSources") @DefaultValue(value = "true") boolean filterEvilSources,
			@QueryParam("maxPage") Integer maxPage) {
		Logging logging = new Logging();
		logging.createEntry(String.format(" GET [excel] keyword=%s", keyword));

		List<DataDto> data = jishoIntegration.read(keyword, maxPage, logging);

		contentFilter.filter(data, filterEvilSources, logging);

		return data;
	}

	@GET
	@Path("cachedData")
	@Produces("application/json")
	public List<SimplifiedJishoDataDto> getCachedData() {
		List<SimplifiedJishoDataDto> data = jishoIntegration.getCachedData();
		return data;
	}

	@GET
	@Path("resetCache")
	public void resetCache() {
		jishoIntegration.resetCache();
	}

	@GET
	@Path("excel")
	@Produces("application/vnd.ms-excel")
	public Response abc(@QueryParam("keyword") String keyword, @QueryParam("filterEvilSources") @DefaultValue(value = "true") boolean filterEvilSources,
			@QueryParam("maxPage") Integer maxPage) {

		Logging logging = new Logging();
		logging.createEntry(String.format(" GET [excel] keyword=%s", keyword));

		List<DataDto> data = jishoIntegration.read(keyword, maxPage, logging);

		DataSpecification specification = calculator.calculate(data, logging);

		contentFilter.filter(data, filterEvilSources, logging);

		List<ExcelEntry> excelData = modelConverter.convert(data, logging);

		posTransformer.initialize(logging);
		posTransformer.createModifiedPOS(excelData);

		try {
			ByteArrayOutputStream outputStream = excelWriter.createWorkbook(keyword, excelData, specification, logging);

			ResponseBuilder responseBuilder = Response.ok(outputStream.toByteArray());

			return responseBuilder.header("Content-Disposition", "inline; filename=jisho-translations-" + keyword + ".xls").build();
		}
		catch (Exception e) {
			e.printStackTrace();
			return Response.serverError().build();
		}
	}
}
