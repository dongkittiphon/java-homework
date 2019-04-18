package com.wongnai.interview.movie.search;

import java.security.Key;
import java.util.*;

import com.wongnai.interview.movie.external.MovieData;
import com.wongnai.interview.movie.external.MovieDataService;
import com.wongnai.interview.movie.external.MoviesResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.wongnai.interview.movie.Movie;
import com.wongnai.interview.movie.MovieRepository;
import com.wongnai.interview.movie.MovieSearchService;

@Component("invertedIndexMovieSearchService")
@DependsOn("movieDatabaseInitializer")
public class InvertedIndexMovieSearchService implements MovieSearchService {
    @Autowired
    private MovieRepository movieRepository;

    @Override
    public List<Movie> search(String queryText) {
        //TODO: Step 4 => Please implement in-memory inverted index to search movie by keyword.
        // You must find a way to build inverted index before you do an actual search.
        // Inverted index would looks like this:
        // -------------------------------
        // |  Term      | Movie Ids      |
        // -------------------------------
        // |  Star      |  5, 8, 1       |
        // |  War       |  5, 2          |
        // |  Trek      |  1, 8          |
        // -------------------------------
        // When you search with keyword "Star", you will know immediately, by looking at Term column, and see that
        // there are 3 movie ids contains this word -- 1,5,8. Then, you can use these ids to find full movie object from repository.
        // Another case is when you find with keyword "Star War", there are 2 terms, Star and War, then you lookup
        // from inverted index for Star and for War so that you get movie ids 1,5,8 for Star and 2,5 for War. The result that
        // you have to return can be union or intersection of those 2 sets of ids.
        // By the way, in this assignment, you must use intersection so that it left for just movie id 5.

        // Building Index
        List<Movie> movies = movieRepository.getAllMovies();
        HashMap<String, HashSet<Long>> index = new HashMap<String, HashSet<Long>>();
        for (int i = 0; i < movies.size(); i++) {
            String[] words = movies.get(i).getName().split("\\W+");
            for (String word : words) {
                word = word.toLowerCase();
                if (!index.containsKey(word)) {
                    index.put(word, new HashSet<Long>());
                }
                index.get(word).add(movies.get(i).getId());
            }
        }

        // Print index map
//		for (Map.Entry<String, HashSet<Long>> entry : index.entrySet()){
//			String key = entry.getKey();
//			HashSet<Long> values = entry.getValue();
//			System.out.println("Key = " + key);
//			System.out.println("Values = " + values + "n");
//		}

        // Search
        List<Movie> result = new ArrayList<Movie>();
        String[] words = queryText.split("\\W+");
        if (index.get(words[0].toLowerCase()) == null) return new ArrayList<Movie>();
        Set<Long> tmp = new HashSet<Long>(index.get(words[0].toLowerCase()));

        for (String word : words) {
            word = word.toLowerCase();
            for (Map.Entry<String, HashSet<Long>> entry : index.entrySet()) {
                String key = entry.getKey();
                HashSet<Long> values = entry.getValue();
                if (word.equals(key)) {
                    tmp.retainAll(values);
                }
            }
        }

        for (Long id : tmp) {
            result.add(movies.get(id.intValue() - 1));
        }

        return result;
    }
}
