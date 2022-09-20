package br.com.zup.edu.nossalojavirtual.products;

import org.hibernate.validator.constraints.URL;

import javax.persistence.Embeddable;
import javax.validation.constraints.NotBlank;

import static org.springframework.util.Assert.hasText;

@Embeddable
public
class Photo {

    @URL
    @NotBlank
    private String url;

    public Photo() {
    }

    /**
     * @param url where photo is stored
     */
    public Photo(String url) {
        hasText(url, "Photo must have an url");
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
