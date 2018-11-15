__author__ = 'Michael'

from os import path
import os

ROOT_FOLDER = "C:/Users/michael.stein/Documents/Repositories/maki-peerfact-moflon-integration-publication/LCN2015/figures/plots"

PLOT_DIRECTORY_PATH = path.join(ROOT_FOLDER, "plots/")

AGGREGATED_CSV_FILE = os.path.join(ROOT_FOLDER, "aggregated_results.csv")

METRICS = ["AverageUnderlayDegree", "AvgUnderlayEdgeLength", "MaxMaxUnderlayEdgeLength",
           "AvgMaxUnderlayEdgeLength", "TotalLinkLengthRelativeInitial",

           "HopSpannerMaxToBase", "HopSpannerMaxPairwise",  # max spanners
           "LengthSpannerMaxToBase", "LengthSpannerMaxPairwise",
           "PowerAlpha2.0SpannerMaxToBase", "PowerAlpha2.0SpannerMaxPairwise",

           "HopSpannerAverageToBase", "HopSpannerAveragePairwise",  # avg spanners
           "LengthSpannerAverageToBase", "LengthSpannerAveragePairwise",
           "PowerAlpha2.0SpannerAverageToBase", "PowerAlpha2.0SpannerAveragePairwise"
           ]

CSV_SEPARATOR = ";"