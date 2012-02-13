package com.ilyasturkben.fileserver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelFutureProgressListener;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.DefaultFileRegion;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.FileRegion;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.util.CharsetUtil;

import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * Main {@link ChannelHandler} that handles listing of directories and serve
 * static files with a default content-type binary/octet-stream.
 * 
 * @author ilyas
 */
public class HttpStaticFileServerHandler extends SimpleChannelUpstreamHandler {
	private final String basePath;
	private final URI baseURI;
	private final Template template;

	public HttpStaticFileServerHandler(File basePath, Template template) {
		this.basePath = basePath.getAbsolutePath();
		this.baseURI = basePath.toURI();
		this.template = template;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
		HttpRequest request = (HttpRequest) e.getMessage();
		if (request.getMethod().equals(HttpMethod.GET)) {
			URI uri;
			try {
				uri = new URI(basePath + request.getUri()).normalize();
			} catch (URISyntaxException ex) {
				sendKo(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
				return;
			}
			File file = new File(uri.toString());
			if (file.isDirectory()) {
				Writer out = new StringWriter();
				Map<String, Object> root = new HashMap<String, Object>();
				root.put("currentPath", request.getUri());
				List<FileBean> files = new ArrayList<FileBean>();
				if (!request.getUri().equals("/")) {
					files.add(new FileBean(file.getParentFile(), baseURI, ".."));
				}
				for (File fileItem : file.listFiles()) {
					FileBean fileBean = new FileBean(fileItem, baseURI, null);
					files.add(fileBean);
				}
				root.put("files", files);
				try {
					template.process(root, out);
					out.flush();
					sendOK(ctx, request, out.toString());
				} catch (IOException ex) {
					sendKo(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
				} catch (TemplateException ex) {
					sendKo(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
				}
			} else {
				try {
					sendFile(ctx, e.getChannel(), request, file);
				} catch (IOException e1) {
					sendKo(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
				}
				// FIXME
			}
		} else if (request.getMethod().equals(HttpMethod.DELETE)
				&& ctx.getChannel().getRemoteAddress().toString()
						.startsWith("/127.0.0.1")
				&& request.getUri().equals("/")
				&& request.getContent().readableBytes() == 0) {
			sendOK(ctx, request, "OK");
			System.exit(0);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		e.getCause().printStackTrace();
		e.getChannel().close();
	}

	private void sendOK(ChannelHandlerContext ctx, HttpRequest request,
			String content) {
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
				HttpResponseStatus.OK);
		response.setHeader(HttpHeaders.Names.CONTENT_TYPE,
				"text/html; charset=UTF-8");
		response.setContent(ChannelBuffers.copiedBuffer(content,
				CharsetUtil.UTF_8));
		ChannelFuture writeFuture = ctx.getChannel().write(response);
		if (!HttpHeaders.isKeepAlive(request)) {
			// keep alive implementation according Netty's docs
			writeFuture.addListener(ChannelFutureListener.CLOSE); 
		}

	}

	private void sendKo(ChannelHandlerContext ctx, HttpResponseStatus status) {
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
				status);
		ctx.getChannel().write(response);
	}

	private void sendFile(ChannelHandlerContext ctx, Channel ch,
			HttpRequest request, File file) throws IOException {

		// original code from netty examples
		RandomAccessFile raf;
		try {
			raf = new RandomAccessFile(file, "r");
		} catch (FileNotFoundException fnfe) {
			sendKo(ctx, HttpResponseStatus.NOT_FOUND);
			return;
		}
		long fileLength = raf.length();

		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
				HttpResponseStatus.OK);
		response.setHeader(HttpHeaders.Names.CONTENT_LENGTH, fileLength);
		// TODO implement ContentType handling to implement
		// response.setHeader(HttpHeaders.Names.CONTENT_TYPE,
		// getContentType(file));
		ch.write(response);
		ChannelFuture writeFuture;
		final FileRegion region = new DefaultFileRegion(raf.getChannel(), 0,
				fileLength);
		writeFuture = ch.write(region);
		writeFuture.addListener(new ChannelFutureProgressListener() {
			@Override
			public void operationComplete(ChannelFuture future) {
				region.releaseExternalResources();
			}

			@Override
			public void operationProgressed(ChannelFuture future, long amount,
					long current, long total) {
			}
		});
		if (!HttpHeaders.isKeepAlive(request)) {
			writeFuture.addListener(ChannelFutureListener.CLOSE);
		}
	}

}