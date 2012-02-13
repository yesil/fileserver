package com.ilyasturkben.fileserver;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ilyasturkben.fileserver.HttpStaticFileServer;

public class HttpStaticFileServerTest {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(HttpStaticFileServerTest.class);

	private static int port;
	private static HttpStaticFileServer server;
	private static File tempFile;

	@BeforeClass
	public static void before() throws IOException, InterruptedException {
		File tempDir = new File(System.getProperty("java.io.tmpdir"))
				.getAbsoluteFile();
		port = 5050;
		server = new HttpStaticFileServer();
		tempFile = new File(tempDir, "static_dummy_file.tmp");
		if (!tempFile.exists()) {
			assertTrue("can't create temporary test file !",
					tempFile.createNewFile());
		}
		LOGGER.debug("Temp file location {}", tempFile.getAbsolutePath());
		File baseDirectory = tempFile.getParentFile();
		server.startServer(port, baseDirectory);
	}

	@Test
	public void startServer() throws URISyntaxException, IOException {
		URL url = new URL("HTTP", "127.0.0.1", port, "/" + tempFile.getName());
		LOGGER.debug("Getting {}", url.toURI().toString());
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		assertEquals(HttpResponseStatus.OK.getCode(), conn.getResponseCode());
		url = new URL("HTTP", "127.0.0.1", port, "/thisfiledoesntexist.txt");
		LOGGER.debug("Getting {}", url.toURI().toString());
		conn = (HttpURLConnection) url.openConnection();
		assertEquals(HttpResponseStatus.NOT_FOUND.getCode(),
				conn.getResponseCode());

	}

	@Ignore
	@Test
	public void stopServer() throws MalformedURLException, IOException {
		// FIXME find another solution to shutdown the server so that this test
		// passe
		URL url = new URL("HTTP", "17.0.0.1", port, "");
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setReadTimeout(1000);
		conn.setRequestMethod("DELETE");
		assertEquals(HttpResponseStatus.OK.getCode(), conn.getResponseCode());
	}
}
