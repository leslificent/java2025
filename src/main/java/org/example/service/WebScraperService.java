
package org.example.service;

import org.example.entity.Listing;
import org.example.repository.ListingRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class WebScraperService {
    private final ListingRepository listingRepository;

    public WebScraperService(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    public void scrapeData() {
        String url = "https://www.olx.ua/d/uk/nedvizhimost/kvartiry/dnepr/";

        try {
            Document doc = Jsoup.connect(url).get();
            Elements listings = doc.select("div[data-cy='l-card']");

            for (Element listing : listings) {
                // Update the selector to correctly target the title element
                String title = listing.select("h6").text(); // Adjust this if necessary
                if (title.isEmpty()) {
                    title = "No Title Available"; // Provide a default title if it's empty
                }
                String price = listing.select("p").text();
                String link = listing.select("a").attr("href");

                Listing newListing = new Listing(null, title, price, link);
                listingRepository.save(newListing);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public List<Listing> getAllListings() {
        return listingRepository.findAll();
    }
}
