/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author MINH_QUAN
 */
@Entity
@Table(name = "stock_alerts", catalog = "semiconductor_inventory_db", schema = "dbo")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "StockAlerts.findAll", query = "SELECT s FROM StockAlerts s"),
    @NamedQuery(name = "StockAlerts.findByAlertId", query = "SELECT s FROM StockAlerts s WHERE s.alertId = :alertId"),
    @NamedQuery(name = "StockAlerts.findByAlertType", query = "SELECT s FROM StockAlerts s WHERE s.alertType = :alertType"),
    @NamedQuery(name = "StockAlerts.findByDescription", query = "SELECT s FROM StockAlerts s WHERE s.description = :description"),
    @NamedQuery(name = "StockAlerts.findBySeverity", query = "SELECT s FROM StockAlerts s WHERE s.severity = :severity"),
    @NamedQuery(name = "StockAlerts.findByCreatedAt", query = "SELECT s FROM StockAlerts s WHERE s.createdAt = :createdAt"),
    @NamedQuery(name = "StockAlerts.findByProcessed", query = "SELECT s FROM StockAlerts s WHERE s.processed = :processed"),
    @NamedQuery(name = "StockAlerts.findByProcessedAt", query = "SELECT s FROM StockAlerts s WHERE s.processedAt = :processedAt"),
    @NamedQuery(name = "StockAlerts.findByEmailSent", query = "SELECT s FROM StockAlerts s WHERE s.emailSent = :emailSent"),
    @NamedQuery(name = "StockAlerts.findByResolved", query = "SELECT s FROM StockAlerts s WHERE s.resolved = :resolved"),
    @NamedQuery(name = "StockAlerts.findByResolvedAt", query = "SELECT s FROM StockAlerts s WHERE s.resolvedAt = :resolvedAt")})
public class StockAlerts implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "alert_id", nullable = false)
    private Integer alertId;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "alert_type", nullable = false, length = 20)
    private String alertType;
    @Size(max = 2147483647)
    @Column(name = "description", length = 2147483647)
    private String description;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "severity", nullable = false, length = 20)
    private String severity;
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @Basic(optional = false)
    @NotNull
    @Column(name = "processed", nullable = false)
    private boolean processed;
    @Column(name = "processed_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date processedAt;
    @Basic(optional = false)
    @NotNull
    @Column(name = "email_sent", nullable = false)
    private boolean emailSent;
    @Basic(optional = false)
    @NotNull
    @Column(name = "resolved", nullable = false)
    private boolean resolved;
    @Column(name = "resolved_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date resolvedAt;
    @JoinColumn(name = "component_id", referencedColumnName = "component_id", nullable = false)
    @ManyToOne(optional = false)
    private Components componentId;
    @JoinColumn(name = "processed_by", referencedColumnName = "user_id")
    @ManyToOne
    private Users processedBy;

    public StockAlerts() {
    }

    public StockAlerts(Integer alertId) {
        this.alertId = alertId;
    }

    public StockAlerts(Integer alertId, String alertType, String severity, boolean processed, boolean emailSent, boolean resolved) {
        this.alertId = alertId;
        this.alertType = alertType;
        this.severity = severity;
        this.processed = processed;
        this.emailSent = emailSent;
        this.resolved = resolved;
    }

    public Integer getAlertId() {
        return alertId;
    }

    public void setAlertId(Integer alertId) {
        this.alertId = alertId;
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public boolean getProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public Date getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Date processedAt) {
        this.processedAt = processedAt;
    }

    public boolean getEmailSent() {
        return emailSent;
    }

    public void setEmailSent(boolean emailSent) {
        this.emailSent = emailSent;
    }

    public boolean getResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    public Date getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Date resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public Components getComponentId() {
        return componentId;
    }

    public void setComponentId(Components componentId) {
        this.componentId = componentId;
    }

    public Users getProcessedBy() {
        return processedBy;
    }

    public void setProcessedBy(Users processedBy) {
        this.processedBy = processedBy;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (alertId != null ? alertId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof StockAlerts)) {
            return false;
        }
        StockAlerts other = (StockAlerts) object;
        if ((this.alertId == null && other.alertId != null) || (this.alertId != null && !this.alertId.equals(other.alertId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.StockAlerts[ alertId=" + alertId + " ]";
    }
    
}
