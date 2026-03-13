package com.cloudmonitor.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for cost fetch operation results.
 */
public class FetchResultDTO {

    private LocalDate startDate;
    private LocalDate endDate;
    private int recordsFetched;
    private int recordsSaved;
    private int recordsUpdated;
    private LocalDateTime fetchedAt;
    private String status;
    private String message;

    public FetchResultDTO() {}

    public FetchResultDTO(LocalDate startDate, LocalDate endDate, int recordsFetched,
                          int recordsSaved, int recordsUpdated, LocalDateTime fetchedAt,
                          String status, String message) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.recordsFetched = recordsFetched;
        this.recordsSaved = recordsSaved;
        this.recordsUpdated = recordsUpdated;
        this.fetchedAt = fetchedAt;
        this.status = status;
        this.message = message;
    }

    public static FetchResultDTO success(LocalDate start, LocalDate end,
                                         int fetched, int saved, int updated) {
        return new FetchResultDTO(start, end, fetched, saved, updated,
                LocalDateTime.now(), "SUCCESS", "Cost data fetched and stored successfully");
    }

    public static FetchResultDTO error(LocalDate start, LocalDate end, String errorMessage) {
        return new FetchResultDTO(start, end, 0, 0, 0,
                LocalDateTime.now(), "ERROR", errorMessage);
    }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public int getRecordsFetched() { return recordsFetched; }
    public void setRecordsFetched(int recordsFetched) { this.recordsFetched = recordsFetched; }

    public int getRecordsSaved() { return recordsSaved; }
    public void setRecordsSaved(int recordsSaved) { this.recordsSaved = recordsSaved; }

    public int getRecordsUpdated() { return recordsUpdated; }
    public void setRecordsUpdated(int recordsUpdated) { this.recordsUpdated = recordsUpdated; }

    public LocalDateTime getFetchedAt() { return fetchedAt; }
    public void setFetchedAt(LocalDateTime fetchedAt) { this.fetchedAt = fetchedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private LocalDate startDate;
        private LocalDate endDate;
        private int recordsFetched;
        private int recordsSaved;
        private int recordsUpdated;
        private LocalDateTime fetchedAt;
        private String status;
        private String message;

        public Builder startDate(LocalDate startDate) { this.startDate = startDate; return this; }
        public Builder endDate(LocalDate endDate) { this.endDate = endDate; return this; }
        public Builder recordsFetched(int recordsFetched) { this.recordsFetched = recordsFetched; return this; }
        public Builder recordsSaved(int recordsSaved) { this.recordsSaved = recordsSaved; return this; }
        public Builder recordsUpdated(int recordsUpdated) { this.recordsUpdated = recordsUpdated; return this; }
        public Builder fetchedAt(LocalDateTime fetchedAt) { this.fetchedAt = fetchedAt; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder message(String message) { this.message = message; return this; }

        public FetchResultDTO build() {
            return new FetchResultDTO(startDate, endDate, recordsFetched, recordsSaved,
                    recordsUpdated, fetchedAt, status, message);
        }
    }
}
