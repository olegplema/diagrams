package org.plema.models;

import java.util.List;

public record Diagram(List<Variable> variables,
                      List<List<AbstractBlock>> threads) {
}
