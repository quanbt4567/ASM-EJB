package managedbean;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;

@Named
@SessionScoped
public class NavigationController implements Serializable {
    
    private String currentPage = "dashboard";
    
    public String navigateTo(String page) {
        this.currentPage = page;
        return page + "?faces-redirect=true";
    }
    
    public String getCurrentPage() {
        return currentPage;
    }
    
    public void setCurrentPage(String currentPage) {
        this.currentPage = currentPage;
    }
    
    public boolean isCurrentPage(String page) {
        return page.equals(currentPage);
    }
}