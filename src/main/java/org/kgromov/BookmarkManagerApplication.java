package org.kgromov;

import org.kgromov.service.BookmarkParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;

import java.nio.file.Path;
import java.nio.file.Paths;

@EnableCaching
@SpringBootApplication
public class BookmarkManagerApplication {

	@Value("classpath:bookmarks/bookmarks.html")
	private Resource bookmarkFile;

	public static void main(String[] args) {
		SpringApplication.run(BookmarkManagerApplication.class, args);
	}

	@Profile("debug")
	@Bean
	ApplicationRunner applicationRunner(BookmarkParser bookmarkParser) {
		return args -> {
			Path bookmarkPath = Paths.get(bookmarkFile.getURI());
			bookmarkParser.parseBookmarksTree(bookmarkPath);
		};
	}
}
