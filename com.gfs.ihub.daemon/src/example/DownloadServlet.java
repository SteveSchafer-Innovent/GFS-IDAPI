package example;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DownloadServlet extends HttpServlet {

	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws IOException, ServletException {
		//
		String directory =
			System.getProperty("java.io.tmpdir")
				+ System.getProperty("file.separator");

		//
		String filename = request.getParameter("filename");
		String contentType = request.getParameter("contentType");

		//
		if (null == filename)
			filename = "noname00.pas";
		if (null == contentType) {
			contentType = "application/download";
			response.setHeader(
				"Content-Disposition",
				"inline; filename=\"" + filename + "\"");
		}

		//
		response.setContentType(contentType);

		System.out.println("Content-type: " + contentType);

		//        
		OutputStream out;
		String fileName;
		String content = filename;
		byte[] data = new byte[1024];

		FileInputStream is;
		is = new FileInputStream(directory + filename);
		out = response.getOutputStream();

		System.out.println("Filename : " + directory + filename);

		while (true) {
			int readCount = is.read(data);
			if (readCount <= 0)
				break;
			out.write(data, 0, readCount);
		}

	}

}
