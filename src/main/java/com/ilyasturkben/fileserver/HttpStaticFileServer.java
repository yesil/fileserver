package com.ilyasturkben.fileserver;

import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelException;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * Main file server class
 * 
 * @author ilyas Stéphane Türkben
 * 
 */
public class HttpStaticFileServer {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(HttpStaticFileServer.class);

	public static void main(String[] args) throws InterruptedException {
		if (args.length > 0) {
			HttpStaticFileServer server = new HttpStaticFileServer();
			CommandEnum command = CommandEnum.valueOf(args[0]);
			int port = 6060;
			if (args.length > 1) {
				try {
					port = Integer.parseInt(args[1]);
				} catch (NumberFormatException e) {
					server.endWithError("port cannot be parsed", e);
				}
			}
			switch (command) {
			case start:
				try {
					String baseDir = args.length > 2 ? args[2] : ".";
					server.startServer(port, new File(baseDir));
				} catch (IOException e) {
					server.endWithError("I/O problem occured !", e);
				}
				break;
			case stop:
				try {
					server.stopServer(port);
				} catch (IOException e) {
					server.endWithError("I/O problem occured !", e);
				}
				break;
			default:
			}

		} else {
			System.out.println("commands availables:");
			System.out.println("start [port] [base directory]");
			System.out.println("ex: start 8080 /tmp");
			System.out.println("stop [port]");
			System.out.println("ex: stop 8080");
		}
	}

	private void endWithError(String message, Throwable e) {
		System.err.println(message);
		if (e != null) {
			e.printStackTrace();
		}
		System.exit(-1);
	}

	public void startServer(int port, File rootDir)
			throws InterruptedException, IOException {
		final HttpStaticFileServerHandler staticFileHandler = new HttpStaticFileServerHandler(
				rootDir, getTemplate());
		NioServerSocketChannelFactory factory = new NioServerSocketChannelFactory(
				Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool());
		ServerBootstrap server = new ServerBootstrap(factory);
		server.setPipelineFactory(new ChannelPipelineFactory() {
			public ChannelPipeline getPipeline() {
				ChannelPipeline pipeline = Channels.pipeline(
						new HttpRequestDecoder(),
						new HttpChunkAggregator(65536),
						new HttpResponseEncoder(), new ChunkedWriteHandler(),
						staticFileHandler);
				return pipeline;
			}
		});
		try {
			server.setOption("reuseAddress", true);
			server.setOption("child.tcpNoDelay", true); // FIXME
			server.setOption("child.keepAlive", true); // FIXME
			server.bind(new InetSocketAddress(port));
		} catch (ChannelException e) {
			if (e.getCause() instanceof BindException) {
				endWithError(e.getCause().getLocalizedMessage(), null);
			}
		}
		LOGGER.info("Listening on port {}", port);
		LOGGER.info("Server started with {} as base directory",
				rootDir.getAbsolutePath());
	}

	public void stopServer(int port) throws MalformedURLException, IOException {
		URL url = new URL("HTTP", "127.0.0.1", port, "");
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("DELETE");
		conn.getResponseCode();
	}

	private Template getTemplate() throws IOException {
		Configuration configuration = new Configuration();
		configuration.setDirectoryForTemplateLoading(new File("templates"));
		return configuration.getTemplate("list.ftl");
	}
}
