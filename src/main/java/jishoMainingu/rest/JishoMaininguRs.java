package jishoMainingu.rest;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.springframework.stereotype.Component;

import jishoMainingu.backend.jisho.JishoAccess;
import jishoMainingu.backend.jisho.model.ResultDto;
import jishoMainingu.function.excel.ExcelWriter;

@Component
@Path("/")
public class JishoMaininguRs {

	@Inject
	private JishoAccess jishoAccess;

	@Inject
	private ExcelWriter excelWriter;

	@GET
	public String useage() {
		StringBuilder message = new StringBuilder();
		message.append("<html>");
		message.append("<body>");
		message.append("Anzeige von JSON: <a target=\"_blank\" href=\"http://localhost:8080/json?keyword=keyword\">http://localhost:8080/json?keyword=keyword</a>");
		message.append("<p/>");
		message.append("Export nach Excel:  <a target=\"_blank\" href=\"http://localhost:8080/excel?keyword=keyword\">http://localhost:8080/excel?keyword=keyword</a>");
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
	public ResultDto raw(@QueryParam("keyword") String keyword) {
		System.out.println(LocalDateTime.now() + " GET [raw] keyword=" + keyword);
		System.out.println("    ask Jisho ...");
		ResultDto result = jishoAccess.read(keyword);
		System.out.println("    ask Jisho ...done");
		System.out.println("    Return result");
		return result;
	}

	@GET
	@Path("excel")
	@Produces("application/vnd.ms-excel")
	public Response abc(@QueryParam("keyword") String keyword) {
		System.out.println(LocalDateTime.now() + " GET [excel] keyword=" + keyword);
		System.out.println("    ask Jisho ...");
		ResultDto result = jishoAccess.read(keyword);
		System.out.println("    ask Jisho ...done");

		try {
			System.out.println("    create Excel ...");
			ByteArrayOutputStream outputStream = excelWriter.createWorkbook(keyword, result);
			System.out.println("    create Excel ...done");

			ResponseBuilder responseBuilder = Response.ok(outputStream.toByteArray());

			responseBuilder.header("Content-Disposition", "inline; filename=jisho-translations-" + keyword + ".xls");

			System.out.println("    Return result");
			return responseBuilder.build();
		}
		catch (Exception e) {
			e.printStackTrace();
			return Response.serverError().build();
		}
	}
}
