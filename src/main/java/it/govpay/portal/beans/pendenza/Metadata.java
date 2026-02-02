/*
 * GovPay - Porta di Accesso al Nodo dei Pagamenti SPC
 * http://www.gov4j.it/govpay
 *
 * Copyright (c) 2014-2026 Link.it srl (http://www.link.it).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3, as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package it.govpay.portal.beans.pendenza;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Metadata Custom da inserire nella ricevuta di pagamento
 **/
@JsonPropertyOrder({
  "mapEntries",
})
public class Metadata {

  @JsonProperty("mapEntries")
  private List<MapEntry> mapEntries = null;

  /**
   **/
  public Metadata mapEntries(List<MapEntry> mapEntries) {
    this.mapEntries = mapEntries;
    return this;
  }

  @JsonProperty("mapEntries")
  public List<MapEntry> getMapEntries() {
    return mapEntries;
  }

  public void setMapEntries(List<MapEntry> mapEntries) {
    this.mapEntries = mapEntries;
  }

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Metadata metadata = (Metadata) o;
    return Objects.equals(mapEntries, metadata.mapEntries);
  }

  @Override
  public int hashCode() {
    return Objects.hash(mapEntries);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Metadata {\n");
    sb.append("    mapEntries: ").append(toIndentedString(mapEntries)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
