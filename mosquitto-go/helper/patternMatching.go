package helper

import (
	"fmt"
	"regexp"
)

func IsTopicMatch(pattern, topic string) bool {
	re, err := regexp.Compile(pattern)
	if err != nil {
		fmt.Println("Error compiling regex:", err)
		return false
	}

	match := re.FindString(topic)
	return match == topic
}