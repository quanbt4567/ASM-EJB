/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entity;

import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;

/**
 *
 * @author MINH_QUAN
 */
@Entity
@Table(name = "components", catalog = "semiconductor_inventory_db", schema = "dbo", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"part_number"})})
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Components.findAll", query = "SELECT c FROM Components c"),
    @NamedQuery(name = "Components.findByComponentId", query = "SELECT c FROM Components c WHERE c.componentId = :componentId"),
    @NamedQuery(name = "Components.findByName", query = "SELECT c FROM Components c WHERE c.name = :name"),
    @NamedQuery(name = "Components.findByCategory", query = "SELECT c FROM Components c WHERE c.category = :category"),
    @NamedQuery(name = "Components.findByDescription", query = "SELECT c FROM Components c WHERE c.description = :description"),
    @NamedQuery(name = "Components.findByPartNumber", query = "SELECT c FROM Components c WHERE c.partNumber = :partNumber"),
    @NamedQuery(name = "Components.findByManufacturer", query = "SELECT c FROM Components c WHERE c.manufacturer = :manufacturer"),
    @NamedQuery(name = "Components.findByQuantity", query = "SELECT c FROM Components c WHERE c.quantity = :quantity"),
    @NamedQuery(name = "Components.findByReorderLevel", query = "SELECT c FROM Components c WHERE c.reorderLevel = :reorderLevel"),
    @NamedQuery(name = "Components.findByUnitPrice", query = "SELECT c FROM Components c WHERE c.unitPrice = :unitPrice"),
    @NamedQuery(name = "Components.findByCurrency", query = "SELECT c FROM Components c WHERE c.currency = :currency"),
    @NamedQuery(name = "Components.findByLocation", query = "SELECT c FROM Components c WHERE c.location = :location"),
    @NamedQuery(name = "Components.findByDatasheetUrl", query = "SELECT c FROM Components c WHERE c.datasheetUrl = :datasheetUrl"),
    @NamedQuery(name = "Components.findByActive", query = "SELECT c FROM Components c WHERE c.active = :active"),
    @NamedQuery(name = "Components.findByCreatedAt", query = "SELECT c FROM Components c WHERE c.createdAt = :createdAt"),
    @NamedQuery(name = "Components.findByUpdatedAt", query = "SELECT c FROM Components c WHERE c.updatedAt = :updatedAt")})
public class Components implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "component_id", nullable = false)
    private Integer componentId;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "name", nullable = false, length = 255)
    private String name;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "category", nullable = false, length = 50)
    private String category;
    @Size(max = 2147483647)
    @Column(name = "description", length = 2147483647)
    private String description;
    @Size(max = 100)
    @Column(name = "part_number", length = 100)
    private String partNumber;
    @Size(max = 100)
    @Column(name = "manufacturer", length = 100)
    private String manufacturer;
    @Basic(optional = false)
    @NotNull
    @Column(name = "quantity", nullable = false)
    private int quantity;
    @Basic(optional = false)
    @NotNull
    @Column(name = "reorder_level", nullable = false)
    private int reorderLevel;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "unit_price", precision = 10, scale = 2)
    private BigDecimal unitPrice;
    @Size(max = 3)
    @Column(name = "currency", length = 3)
    private String currency;
    @Size(max = 50)
    @Column(name = "location", length = 50)
    private String location;
    @Size(max = 500)
    @Column(name = "datasheet_url", length = 500)
    private String datasheetUrl;
    @Basic(optional = false)
    @NotNull
    @Column(name = "active", nullable = false)
    private boolean active;
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
    @JoinColumn(name = "created_by", referencedColumnName = "user_id")
    @ManyToOne
    private Users createdBy;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "componentId")
    private Collection<StockAlerts> stockAlertsCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "componentId")
    private Collection<ReorderRequests> reorderRequestsCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "componentId")
    private Collection<Transactions> transactionsCollection;

    public Components() {
    }

    public Components(Integer componentId) {
        this.componentId = componentId;
    }

    public Components(Integer componentId, String name, String category, int quantity, int reorderLevel, boolean active) {
        this.componentId = componentId;
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.reorderLevel = reorderLevel;
        this.active = active;
    }

    public Integer getComponentId() {
        return componentId;
    }

    public void setComponentId(Integer componentId) {
        this.componentId = componentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getReorderLevel() {
        return reorderLevel;
    }

    public void setReorderLevel(int reorderLevel) {
        this.reorderLevel = reorderLevel;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDatasheetUrl() {
        return datasheetUrl;
    }

    public void setDatasheetUrl(String datasheetUrl) {
        this.datasheetUrl = datasheetUrl;
    }

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Users getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Users createdBy) {
        this.createdBy = createdBy;
    }

    @XmlTransient
    public Collection<StockAlerts> getStockAlertsCollection() {
        return stockAlertsCollection;
    }

    public void setStockAlertsCollection(Collection<StockAlerts> stockAlertsCollection) {
        this.stockAlertsCollection = stockAlertsCollection;
    }

    @XmlTransient
    public Collection<ReorderRequests> getReorderRequestsCollection() {
        return reorderRequestsCollection;
    }

    public void setReorderRequestsCollection(Collection<ReorderRequests> reorderRequestsCollection) {
        this.reorderRequestsCollection = reorderRequestsCollection;
    }

    @XmlTransient
    public Collection<Transactions> getTransactionsCollection() {
        return transactionsCollection;
    }

    public void setTransactionsCollection(Collection<Transactions> transactionsCollection) {
        this.transactionsCollection = transactionsCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (componentId != null ? componentId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Components)) {
            return false;
        }
        Components other = (Components) object;
        if ((this.componentId == null && other.componentId != null) || (this.componentId != null && !this.componentId.equals(other.componentId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.Components[ componentId=" + componentId + " ]";
    }
    
}
